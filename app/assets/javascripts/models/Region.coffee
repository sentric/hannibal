# Copyright 2013 Sentric. See LICENSE for details.

class @Region extends Backbone.Model
  isZeroLength: () ->
    @get('storefileSizeMB') == 0

class @Regions extends Backbone.Collection
  url: Routes.Regions.listJson()
  model: Region

  @sortFunctions =
    'size': (region) -> 1/region.get('storefileSizeMB')
    'host': (a, b) ->
      if a.get('serverName') == b.get('serverName')
        if a.get('startKey') > b.get('startKey')
          1
        else
          -1
      else
        if a.get('serverName') > b.get('serverName')
          1
        else
          -1

  @byTable: (tableName) ->
    regions = new Regions([])
    regions.url = Routes.Regions.listJson
      table: tableName
    regions

  initialize: () ->
    super()
    @setSort("size")

  setSort: (attrib) ->
    func = @constructor.sortFunctions[attrib]
    if !func
      func = (a, b) ->
        if a.get(attrib) > b.get(attrib)
          1
        else
          -1
    @comparator = func

  groupByAttribute: (attr) ->
    @groupBy (regionInfo) -> regionInfo.get(attr)

  ofTable: (table) ->
    @select (regionInfo) -> regionInfo.get('tableName') == table

  isZeroLength: () ->
    @all((region) -> region.isZeroLength())