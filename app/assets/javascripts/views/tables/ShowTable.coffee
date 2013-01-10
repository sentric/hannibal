# Copyright 2012 Sentric. See LICENSE for details.

class @ShowTableView extends Backbone.View

  initialize: ->
    @palette = new RickshawUtil.TablePalette()
    @tableRegionsChartView = @createTableRegionsChartView($(".table-regions-chart-view"))

    @visualCountDown = new VisualCountDownView
      el: @$(".refresh-text")
      pattern: "(next refresh in %delay% seconds)"
    @visualCountDown.on "done", _.bind(@updateTableRegions, @)

    @updateTableRegions()

  createTableRegionsChartView: ($el) ->
    table = $el.data("table")
    regions = Regions.byTable(table.name)
    tableRegionsChartView = new TableRegionsChartView
      el: $el
      table: table
      palette: @palette
      collection: regions
    tableRegionsChartView

  updateTableRegions: ->
    @tableRegionsChartView.collection.fetch()
    @visualCountDown.startCountDown(30, 1, 1000)

