package models

import utils.ByteUtil
import java.security.MessageDigest
import org.apache.hadoop.hbase.util.Bytes
import scala.collection.mutable
import anorm._
import play.api.db.DB
import play.api.Logger
import play.api.Play.current

/**
 * Created by nkuebler on 20/05/14.
 */
case class RegionHash(name:String, hash:String) {
}

object RegionHash {

  def byName(name:String) = {
    val hash:String = nameToHash.get(name).getOrElse {
      val hashedName = this.hash(name)
      DB.withConnection { implicit c =>
        val stream = SQL_FIND_NAME.on("hash" -> hashedName)()
        if(stream.isEmpty) { // add record if not yet exists
          SQL_INSERT_NAME.on("hash" -> hashedName, "name" -> name).executeInsert()
        }
      }
      hashToName.put(hashedName, name)
      nameToHash.put(name, hashedName)
      hashedName
    }
    RegionHash(name, hash)
  }

  def byHash(hash:String) : RegionHash = {
    val name = hashToName.get(hash).getOrElse {
      DB.withConnection { implicit c =>
        val stream = SQL_FIND_NAME.on("hash" -> hash)()
        if(stream.isEmpty) {
          Logger.info("no region found for hash: " +hash)
          "-unknown-"
        } else {
          val row = stream.head
          val name = row[String]("name")
          hashToName.put(hash, name)
          nameToHash.put(name, hash)
          name
        }
      }
    }
    RegionHash(name, hash)
  }

  def hash(value:String) = ByteUtil.toHexString(MessageDigest.getInstance("MD5").digest(Bytes.toBytes(value)))

  val hashToName:mutable.HashMap[String, String] = new mutable.HashMap()
  val nameToHash:mutable.HashMap[String, String] = new mutable.HashMap()

  val SQL_FIND_NAME = SQL("""
    SELECT
      name
    FROM
      region
    WHERE
      hash={hash}
  """)

  val SQL_INSERT_NAME = SQL("""
    INSERT INTO
      region(hash, name)
    VALUES
      ({hash}, {name})
  """)
}
