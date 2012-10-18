class @RegionMetricChartView extends Backbone.View
  initialize: ->
    @regionIdTimestamp = @$el.data("region-id-timestamp")
    @metricNames = @$el.data("metric-names")
    @palette = @options.palette
    @collection.on "reset", _.bind(@render, @)

  render: ->

    if(@collection.isEmpty())
      @$el.html("No Data recorded yet for MetricDef #{@metricNames}")
    else
      @series = @collection
        .filter((metric) => !metric.isEmpty())
        .map((metric) => new MetricSeries(metric, @palette.color()))
      compactionsSeries = @findSeries("compactions")
      compactionsSeries.disabled = true if compactionsSeries && @series.length > 1

      @graph =  new Rickshaw.Graph
        element: @$(".chart")[0],
        renderer: 'line',
        series: @series
        interpolation: 'linear'

      @hoverDetail = new Rickshaw.Graph.HoverDetail
        graph: @graph
        yFormatter: ((y) => y)
        formatter: ((series, x, y, formattedX, formattedY, d) =>
          "#{series.name} : #{series.denormalize(y)}"
        )

      time = new Rickshaw.Fixtures.Time()
      @xAxis = new RickshawUtil.LeftAlignedXAxis
        graph: @graph
        element: @$(".x-axis")[0]
        tickFormat: (x) ->
          d = new Date(x * 1000)
          time.formatTime(d)

      @slider = new Rickshaw.Graph.RangeSlider
        graph: @graph,
        element: @$(".slider")

      @annotator = new Rickshaw.Graph.Annotate
        graph: @graph,
        element: @$('.timeline')[0]

      @createCompactionAnnotations(compactionsSeries) if compactionsSeries

      @legend = new Rickshaw.Graph.Legend
        graph: @graph
        element: @$(".legend")[0]

      @shelving = new Rickshaw.Graph.Behavior.Series.Toggle
        graph: @graph
        legend: @legend

      @graph.render()

      @colorizeAnnotations(compactionsSeries.color) if compactionsSeries

      @trigger "graph_rendered"

      $(".timeline").delegate(".annotation", 'mouseover',( (e) ->
        $(this).trigger("click")
      ));

      $(".timeline").delegate(".annotation", 'mouseout',( (e) ->
        $(this).trigger("click")
      ));

  createCompactionAnnotations: (compactions) ->
    compactions.noLegend = true
    metric = compactions.metric
    values = metric.getValues()
    start = Math.round(metric.getBegin() / 1000)
    _(values).each (v) =>
      if v.v > 0
        start = v.ts
      else
        time = Math.round(start / 1000)
        @annotator.add(time, "Compaction (#{v.ts - start}ms)", Math.round(v.ts / 1000))

  colorizeAnnotations: (color) ->
    for ts, annotation of @annotator.data
      element = annotation.element
      element.style.backgroundColor = color;
      annotation.line.style.backgroundColor = color;
      annotation.boxes.forEach( (box) ->
        if box.rangeElement then box.rangeElement.style.backgroundColor = color;
      )

  findSeries: (name) -> _(@series).find((series) -> series.metric.getName() == name)