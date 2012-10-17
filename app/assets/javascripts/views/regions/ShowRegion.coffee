class @ShowRegionView extends Backbone.View

  initialize: ->
    @palette = new Rickshaw.Color.Palette( { scheme: 'munin' } )
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