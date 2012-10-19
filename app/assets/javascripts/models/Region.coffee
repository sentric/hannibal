# Copyright 2012 Sentric. See LICENSE for details.

class @Region extends Backbone.Model
  isZeroLength: () ->
    @get('storefileSizeMB') == 0

class @Regions extends Backbone.Collection
  url: Routes.Regions.listJson()
  model: Region
  comparator: (region) -> 1/region.get('storefileSizeMB')

  @byTable: (tableName) ->
    regions = new Regions([])
    regions.url = Routes.Regions.listJson
      table: tableName
    regions

  groupByAttribute: (attr) ->
    @groupBy (regionInfo) -> regionInfo.get(attr)

  ofTable: (table) ->
    @select (regionInfo) -> regionInfo.get('tableName') == table

  isZeroLength: () ->
    @all((region) -> region.isZeroLength())