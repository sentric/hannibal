# Copyright 2012 Sentric. See LICENSE for details.

class @ShowRegionView extends Backbone.View

  initialize: ->
    @palette = new Rickshaw.Color.Palette( { scheme: [
      '#B1354A', # Storefiles
      '#B12BA0', # Compactions
      '#68B15D', # Memstore Size
      '#4E5FB1', # Storefile Size
      '#56AFB1', # not used
      '#B1A667', # not used
    ] } )
    @regionMetricCharts = []
    @$(".region-metric-chart-view").each (idx, element) =>
      @regionMetricCharts.push @createRegionMetricChartView(@$(element))

  createRegionMetricChartView: ($el) ->
    metrics = Metrics.byNames($el.data("region-name"), $el.data("metric-names"))
    view = new RegionMetricChartView
      el: $el
      palette: @palette
      collection: metrics
    metrics.fetch()
    view