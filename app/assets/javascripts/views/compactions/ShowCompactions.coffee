# Copyright 2014 YMC. See LICENSE for details.

class @ShowCompactionsView extends Backbone.View

  initialize: ->
    @metricsCharts = []
    @$(".compaction-metric-chart-view").each (idx, element) =>
      @metricsCharts.push @createMetricView(@$(element))

    @visualCountDown = new VisualCountDownView
      el: @$(".refresh-text")
      pattern: "(next refresh in %delay% seconds)"
    @visualCountDown.on "done", _.bind(@updateMetrics, @)

    if @metricsCharts.length > 0
      @compactions = @metricsCharts[0].collection
      @compactions.on "reset", _.bind(@updateLongestCompactionDuration, @)

    @updateMetrics()

  createMetricView: ($el) ->
    metrics = Metrics.byNames( $el.data("metric-names") )
    view = new MetricChartView
      el: $el
      collection: metrics
      metricFilter: (collection) -> collection.groupedByTable()
      doNormalize: false
      renderer: 'area'
      palette: new RickshawUtil.TablePalette()
    view

  updateMetrics: ->
    view.collection.fetch() for view in @metricsCharts
    @visualCountDown.startCountDown(125, 1, 1000)

  updateLongestCompactionDuration: ->
    max = 0
    target = "[none]"
    date = new Date()
    @compactions.each (metric) ->
      begin = metric.begin;
      _(metric.getValues()).each (record) ->
        if(record.v > 0)
          begin = record.ts
        else if record.ts - begin > max
          max = record.ts - begin
          date = new Date(begin)
          target = metric.getTargetDesc()

    route = Routes.Regions.show
      name: target
    @$(".longest-compaction").html("<b>#{(max/1000.0).toFixed(1)}s</b> <a href='#{route}'>on Region</a>")