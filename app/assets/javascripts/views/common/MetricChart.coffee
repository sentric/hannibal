# Copyright 2014 YMC. See LICENSE for details.

class @MetricChartView extends Backbone.View
  initialize: ->
    @palette = @options.palette
    @annotatedMetricName = @options.annotatedMetricName
    @annotationLabel = @options.annotationLabel
    @collection.on "reset", _.bind(@render, @)

    @doNormalize = @options.doNormalize
    @metricsSeries = new MetricsSeries(@doNormalize, @palette)

    if @options.metricFilter
      @metricFilter = @options.metricFilter
    else
      @metricFilter = ((collection) -> collection.models)

    if @options.renderer
      @renderer = @options.renderer
    else
      @renderer = 'line'

  render: ->
    if(@collection.isEmpty())
      @$el.html("No Data recorded yet.")
    else
      metrics = @metricFilter(@collection)
      @metricsSeries.populate(metrics)

      if !@graph
        @createGraph()
      else
        @updateGraph()
      @trigger "graph_rendered"

  createGraph: ->
    @graph =  new Rickshaw.Graph
      element: @$(".chart")[0],
      renderer: @renderer,
      series: @metricsSeries.series
      interpolation: 'linear'

    @hoverDetail = new Rickshaw.Graph.HoverDetail
      graph: @graph
      yFormatter: ((y) => y)
      formatter: ((series, x, y, formattedX, formattedY, d) =>
        "#{series.name} : #{series.denormalize(y)} #{series.unit}"
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

    @createAnnotations()

    @legend = new Rickshaw.Graph.Legend
      graph: @graph
      element: @$(".legend")[0]

    @shelving = new Rickshaw.Graph.Behavior.Series.Toggle
      graph: @graph
      legend: @legend

    @graph.render()

    $(".timeline").delegate(".annotation", 'mouseover',( (e) ->
      $(this).trigger("click")
    ));

    $(".timeline").delegate(".annotation", 'mouseout',( (e) ->
      $(this).trigger("click")
    ));

    @colorizeAnnotations(@annotatedSeries.color) if @annotatedSeries
    @labelYAxes()

  createAnnotations: () ->
    @lastAddedAnnotation = 0
    if @annotatedMetricName
      @annotatedSeries = @metricsSeries.findSeries(@annotatedMetricName)
    @annotatedSeries.disabled = true if @annotatedSeries
    @addAnnotations(@annotatedSeries) if @annotatedSeries

  updateGraph: ->
    @addAnnotations(@annotatedSeries) if @annotatedSeries
    @graph.update()
    @graph.render()
    @colorizeAnnotations(@annotatedSeries.color) if @annotatedSeries
    @labelYAxes()

  addAnnotations: (series) ->
    series.noLegend = true
    metric = series.metric
    values = metric.getValues()
    start = Math.round(metric.getBegin())
    _(values).each (v) =>
      if v.v > 0
        start = v.ts
      else
        time = Math.round(start / 1000)
        if time > @lastAddedAnnotation
          @lastAddedAnnotation = time
          duration = Math.round((v.ts - start) / 1000)
          @annotator.add(time, "#{@annotationLabel} (#{duration}s)", Math.round(v.ts / 1000))

  colorizeAnnotations: (color) ->
    for ts, annotation of @annotator.data
      element = annotation.element
      if(! annotation.element )
        console.log("annotation without element!")
      else
        element.style.backgroundColor = color;
        annotation.line.style.backgroundColor = color;
        annotation.boxes.forEach( (box) ->
          if box.rangeElement then box.rangeElement.style.backgroundColor = color;
        )

  labelYAxes: ->
    if @doNormalize
      _(@metricsSeries.series).each (metricSeries) ->
        name = metricSeries.name
        if metricSeries.metricName != @annotatedMetricName
          $("span:contains('#{name}')").html("#{name}: <br><span class='labelindent'>#{metricSeries.min} - #{metricSeries.max} #{metricSeries.unit}</span>")

