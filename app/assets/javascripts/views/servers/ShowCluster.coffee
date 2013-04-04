# Copyright 2012 Sentric. See LICENSE for details.

class @ShowClusterView extends Backbone.View

  initialize: ->
    regions = new Regions()
    @serverChart = window.serverChart = new ServerChartView
      el: @$("#server_chart")
      collection: regions

    @visualCountDown = new VisualCountDownView
      el: @$(".server-refresh-text")
      pattern: "(next refresh in %delay% seconds)"
    @visualCountDown.on "done", _.bind(@updateClusterRegions, @)

    @clusterMetricCharts = []
    @$(".cluster-metric-chart-view").each (idx, element) =>
      @clusterMetricCharts.push @createMetricView(@$(element))

    @visualCountDown2 = new VisualCountDownView
      el: @$(".refresh-text")
      pattern: "(next refresh in %delay% seconds)"
    @visualCountDown2.on "done", _.bind(@updateMetrics, @)

    @updateClusterRegions()
    @updateMetrics()

  updateClusterRegions: ->
    @serverChart.collection.fetch()
    @visualCountDown.startCountDown(30, 1, 1000)

  createMetricView: ($el) ->
    metrics = Metrics.byNames( $el.data("metric-names") )
    view = new MetricChartView
      el: $el
      collection: metrics
      metricFilter: (collection) -> collection.groupedByTable()
      doNormalize: false
      renderer: 'area'
    view

  updateMetrics: ->
    view.collection.fetch() for view in @clusterMetricCharts
    @visualCountDown2.startCountDown(125, 1, 1000)