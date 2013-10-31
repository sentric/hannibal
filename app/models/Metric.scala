/*
 * Copyright 2013 Sentric. See LICENSE for details.
 */

package models

import anorm._
import play.api.Play.current
import play.api.db.DB
import play.api.Logger
import models.MetricDef._
import collection.mutable.{ListBuffer, MutableList}
import org.apache.hadoop.hbase.util.Bytes
import utils.ByteUtil
import java.security.MessageDigest

object MetricDef {

  val MAX_TARGET_DESC_LENGTH = 1000

  val ALL_REGION_METRICS = Set("storefileSizeMB", "memstoreSizeMB", "storefiles", "compactions")

  val STOREFILE_SIZE_MB = "storefileSizeMB"
  def STOREFILE_SIZE_MB(region:String) : MetricDef = findRegionMetricDef(region, STOREFILE_SIZE_MB)

  val MEMSTORE_SIZE_MB = "memstoreSizeMB"
  def MEMSTORE_SIZE_MB(region:String) : MetricDef = findRegionMetricDef(region, MEMSTORE_SIZE_MB)

  val STOREFILES  = "storefiles"
  def STOREFILES(region:String) : MetricDef  = findRegionMetricDef(region, STOREFILES)

  val COMPACTIONS = "compactions"
  def COMPACTIONS(region: String) : MetricDef = findRegionMetricDef(region, COMPACTIONS)

  def findRegionMetricDef(region: String, name: String) = find(hash(region), name, region)

  def find(target: String, name: String, targetDesc: String)  = {
    DB.withConnection { implicit c =>
      val stream = SQL_FIND_METRIC.on("target" -> target, "name" -> name)()

      val truncatedTargetDesc = if(targetDesc.length > MAX_TARGET_DESC_LENGTH) {
        Logger.warn("truncating long region-name to %d characters".format(MAX_TARGET_DESC_LENGTH))
        targetDesc.substring(0, MAX_TARGET_DESC_LENGTH)
      } else {
        targetDesc
      }

      if(stream.isEmpty) {
        Logger.info("creating new metric for " + target + " : " + name)
        val id = SQL_INSERT_METRIC.on("target" -> target, "name" -> name, "target_desc" -> truncatedTargetDesc).executeInsert()
        MetricDef(id.get, target, name, 0.0, 0, truncatedTargetDesc)
      } else {
        val row = stream.head
        if(row[String]("target_desc") != truncatedTargetDesc)
        {
          Logger.info("updating metric targetDesc in old metric '" + target +"' for " + name)
          SQL_MIGRATE_METRIC_3.on("id" -> row[Long]("id"), "target_desc" -> truncatedTargetDesc).executeUpdate()
        }
        MetricDef(
          row[Long]("id"),
          row[String]("target"),
          row[String]("name"),
          row[Double]("last_value"),
          row[Long]("last_update"),
          truncatedTargetDesc
        )
      }
    }
  }

  def findByName(name: String):Seq[MetricDef] = {
    DB.withConnection { implicit c =>
      val stream = SQL_FIND_METRIC_ALL.on("name" -> name)()

      if(stream.isEmpty) {
        Logger.info("no metrics found for : " + name)
        List()
      } else {
        stream.map( row => {
          MetricDef(
            row[Long]("id"),
            row[String]("target"),
            row[String]("name"),
            row[Double]("last_value"),
            row[Long]("last_update"),
            row[String]("target_desc")
          )
        }).toList
      }
    }
  }

  def clean(until: Long = now() - 1000 * 3600 * 24 * 7) = {
    var recordsCleaned = 0;
    var metricsCleaned = 0;
    DB.withConnection { implicit c =>
      recordsCleaned = SQL_DELETE_RECORDS.on("until" -> until).executeUpdate()
      metricsCleaned = SQL_DELETE_METRICS.on("until" -> until).executeUpdate()
    }
    Tuple2(metricsCleaned, recordsCleaned);
  }

  def now() = new java.util.Date().getTime()

  def hash(value:String) = ByteUtil.toHexString(MessageDigest.getInstance("MD5").digest(Bytes.toBytes(value)))

  val SQL_FIND_METRIC_ALL = SQL("""
    SELECT
      id, target, name, last_value, last_update, target_desc
    FROM
      metric
    WHERE
      name={name}
  """)

  val SQL_FIND_METRIC = SQL("""
    SELECT
      id, target, name, last_value, last_update, target_desc
    FROM
      metric
    WHERE
      target={target} AND name={name}
  """)

  val SQL_INSERT_METRIC = SQL("""
    INSERT INTO
      metric(target, name, last_value, last_update, target_desc)
    VALUES
      ({target}, {name}, 0.0, 0, {target_desc})
  """)

  val SQL_UPDATE_METRIC = SQL("UPDATE metric SET last_value={last_value}, last_update={last_update} WHERE id={id}")

  val SQL_MIGRATE_METRIC_3 = SQL("UPDATE metric SET target_desc={target_desc} WHERE id={id}")

  val SQL_INSERT_RECORD = SQL("""
    INSERT INTO
      record(metric_id, timestamp, prev_value, value)
    VALUES
      ({metric_id}, {timestamp}, {prev_value}, {value})
  """)

  val SQL_FIND_RECORDS = SQL("""
    SELECT
      timestamp, prev_value, value
    FROM
      record
    WHERE
      metric_id = {metric_id} AND timestamp > {since} AND timestamp <= {until}
    ORDER BY
      timestamp
  """)

  val SQL_DELETE_RECORDS = SQL("""
    DELETE FROM
      record
    WHERE
      timestamp < {until}
  """)

  val SQL_DELETE_METRICS = SQL("""
    DELETE FROM
      metric
    WHERE
      last_update < {until}
  """)
}

case class MetricDef(id: Long, target: String, name: String, var lastValue: Double, var lastUpdate: Long, targetDesc:String) {
  def update(value: Double, timestamp:Long = now) = {
    var updated = false
    DB.withConnection { implicit c =>
      if(lastValue != value) {
        SQL_INSERT_RECORD.on("metric_id" -> id, "timestamp" -> timestamp, "prev_value" -> lastValue, "value" -> value).executeInsert()
        lastValue = value
        updated = true
      }
      if(lastUpdate < timestamp) {
        lastUpdate = timestamp
        SQL_UPDATE_METRIC.on("id" -> id, "last_update" -> lastUpdate, "last_value" -> lastValue).executeUpdate()
      }
    }
    updated
  }

  def metric(since: Long, until: Long) : Metric = {
    var values:List[MetricRecord] = null;
    var prevValue:Option[Double] = None;
    DB.withConnection { implicit c =>
      values = SQL_FIND_RECORDS.on("metric_id" -> id, "since" -> since, "until" -> until)().map( row => {
        if (prevValue == None) {
          prevValue = Some(row[Double]("prev_value"))
        }
        MetricRecord(row[Long]("timestamp"), row[Double]("value"));
      }).toList
    }
    if(values.size < 1)
      Metric(name, target, since, until, values, lastValue, lastUpdate == 0, targetDesc)
    else
      Metric(name, target, since, until, values, prevValue.get, lastUpdate == 0, targetDesc)
  }
}

case class Metric(name: String, target: String, begin: Long, end: Long, values: Seq[MetricRecord], prevValue: Double, isEmpty: Boolean, targetDesc:String)

case class MetricRecord(ts: Long, v: Double)



