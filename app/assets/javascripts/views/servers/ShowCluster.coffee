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
      @clusterMetricCharts.push @createClusterMetricChartView(@$(element))

    @updateClusterRegions()
    @updateMetrics()

  updateClusterRegions: ->
    @serverChart.collection.fetch()
    @visualCountDown.startCountDown(30, 1, 1000)

  createClusterMetricChartView: ($el) ->
    metrics = Metrics.byName( $el.data("metric-names")[0] )
    view = new MetricChartView
      el: $el
      collection: metrics
      collectionFilter: (metrics) -> metrics.groupedByName()
      doNormalize: false
    view

  updateMetrics: ->
    view.collection.fetch() for view in @clusterMetricCharts