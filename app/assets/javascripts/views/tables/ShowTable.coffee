# Copyright 2012 Sentric. See LICENSE for details.

class @ShowTableView extends Backbone.View

  initialize: ->
    @palette = new RickshawUtil.TablePalette()
    @tableRegionsChartView = @createTableRegionsChartView($(".table-regions-chart-view"))

  createTableRegionsChartView: ($el) ->
    table = $el.data("table")
    regions = Regions.byTable(table.name)
    tableRegionsChartView = new TableRegionsChartView
      el: $el
      table: table
      palette: @palette
      collection: regions
    regions.fetch()
    tableRegionsChartView