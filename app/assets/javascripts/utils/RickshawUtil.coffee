# Copyright 2012 Sentric. See LICENSE for details.

root = exports ? @
$ = root.jQuery

class @RickshawUtil
  class @TablePalette
    constructor: (options = {}) ->
      options = _.defaults options,
        paletteOptions: {},
        tableColors: root.TableColors

      {paletteOptions, tableColors} = options

      @palette = new Rickshaw.Color.Palette(paletteOptions)
      @tableColors = tableColors

    color: (key) ->
      @tableColors[key] = @palette.color(key) unless @tableColors[key]?
      return @tableColors[key]

  class @LeftAlignedXAxis extends Rickshaw.Graph.Axis.X
    constructor: ->
      super

      oldRender = @render
      @render = ->
        oldRender.apply(@)
        @vis.selectAll('text').attr('text-anchor', 'start')

  class @InteractiveHoverDetail extends Rickshaw.Graph.HoverDetail
    initialize: (args) ->
      @onClick = args["onClick"];
      @onOver = args["onOver"];
      @onOut = args["onOut"];
      args["onRender"] = (renderArgs)=>
        hoveredSeries = _.find(renderArgs.detail, (d)-> d.active)
        if hoveredSeries?
          @hoveredSeries = hoveredSeries
          if typeof @onOver == "function"
            @onOver(hoveredSeries)
          $("body").css("cursor", "pointer")
        else
          @mouseOut()
      args["onHide"] = () =>
        @mouseOut()
      super args

    mouseOut: () ->
      if @hoveredSeries
        @hoveredSeries = null
        if typeof @onOut == "function"
          @onOut()
        $("body").css("cursor", "default")

    _addListeners: () ->
      this.graph.element.addEventListener 'click', (e) =>
        console.log("click")
        if @hoveredSeries
          if typeof @onClick == "function"
            @onClick @hoveredSeries
      super()

    # TODO: contribute this back to Rickshaw.js
    updateGroupedHover: (e) ->
      e ?= @lastEvent
      return  unless e

      @lastEvent = e
      return  unless e.target.nodeName.match(/^(path|svg|rect)$/)

      graph = @graph
      $el = $(@element)

      eventX = e.offsetX or e.layerX
      eventY = e.offsetY or e.layerY
      domainX = Math.floor(graph.x.invert(eventX))
      barsXStart = graph.x(domainX)
      formattedXValue = @xFormatter(domainX)
      activeSeries = graph.series.active()

      seriesBarWidth = graph.renderer.barWidth() / activeSeries.length
      activeSeriesIndex = Math.floor((eventX - barsXStart) / seriesBarWidth)

      # Bail out if the cursor's in the margin between two X values
      return  if activeSeriesIndex > activeSeries.length - 1

      detailXOffset = barsXStart + (activeSeriesIndex * seriesBarWidth)
      detail = for series, index in activeSeries
        data = series.data[domainX]
        {
          series
          formattedYValue: @yFormatter(data.y)
          graphX: barsXStart
          graphY: graph.y(data.y)
          order: index
          name: series.name
          value: data
          active: index == activeSeriesIndex
        }

      $el.html('').css(left: "#{detailXOffset}px")

      if @visible then @render
        detail: detail
        domainX: domainX
        formattedXValue: formattedXValue
        mouseX: eventX
        mouseY: eventY

    update: (e) ->
      if @graph.renderer.unstack
        @updateGroupedHover(e)
      else
        super

  class @ThresholdLine
    constructor: (args) ->
      if args.graph.renderer.name != "bar"
        throw "ThresholdLine only works with 'bar' renderer right now"

      graph = args.graph
      legend = args.legend
      threshold = args.threshold
      name = args.name
      color = args.color
      disabled = args.disabled

      graph.registerRenderer(new ThresholdBarRenderer({graph: graph}))
      graph.setRenderer('thresholdbar')

      splitsize = {
        name: name,
        threshold: threshold,
        color: color,
        disabled: disabled,
        disable: () ->
          @disabled = true
          graph.update()
        enable: () ->
          @disabled = false
          graph.update()
      }

      graph.series.splitsize = splitsize
      legend.addLine(splitsize)

    class ThresholdBarRenderer extends Rickshaw.Graph.Renderer.Bar
      name: 'thresholdbar'

      initialize: (args) ->
        super args

      domain: ($super) ->
        domain = super($super)
        if !@graph.series.splitsize.disabled
          domain.y[1] = Math.max(@graph.series.splitsize.threshold * 1.05, domain.y[1])
        domain

      render: () ->
        super()
        if !@graph.series.splitsize.disabled
          graph = @graph
          nodes = graph.vis.selectAll("path")
            .data([{x:0, y:@graph.series.splitsize.threshold}])
            .enter().append("svg:rect")
            .attr("x", (d) -> graph.x(d.x) )
            .attr("y", (d) -> graph.y(d.y) )
            .attr("width", graph.width)
            .attr("height", 1)
          nodes[0][0].setAttribute("fill", "#ff0000")

  class @AllSeriesToggle extends Backbone.View
    events:
      "click .line": (ev) ->
        if $(ev.target).parents('.all-toggle').length > 0
          @toggleAll()
        
        @render()
      
    constructor: (options = {}) ->
      # This needs to be done before Backbone.View's constructor is run
      # to have a default 'el' option
      {@toggle, toggle: {@graph, @legend}} = options
      _.defaults options,
        el: @legend.list
        toggleText: "All Series"

      super(options)

    initialize: ->
      @render()

    toggleAll: ->
      $('.line.disabled', @legend.element).removeClass('disabled')
      _.chain(@graph.series)
        .select((serie) -> serie.disabled)
        .invoke('enable')

    render: ->
      $toggle = @$('.all-toggle')

      if $toggle.length == 0
        # Use concatenation to avoid introducing whitespaces. Otherwise
        # the layout could be disrupted
        @$el.append "" +
          """<li class="line all-toggle">""" +
            """<a class="action">✔</a>""" +
            """<div class="swatch"></div>""" +
            """<span class="label">#{@options.toggleText}</span>""" +
          """</li>""" +
        ""
        $toggle = @$('.all-toggle')

      $toggle.toggleClass 'disabled', _.any(@graph.series, (series) -> series.disabled)

  @humanReadableBytes: (bytes, minExponent = 0) ->
    prefixes = ["bytes", "kB", "MB", "GB", "TB", "PB"]
    exponent = Math.max(minExponent, @getHumanReadableExponent(bytes))
    return (bytes / Math.pow(1024, Math.floor(exponent))).toFixed(2) + " " + prefixes[exponent]

  @getHumanReadableExponent: (bytes) ->
    Math.floor(Math.log(bytes) / Math.log(1024))