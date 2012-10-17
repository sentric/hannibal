class @RickshawUtil

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
            @onOver hoveredSeries
        else
          @hoveredSeries = null
      args["onHide"] = () =>
        @hoveredSeries = null
        if typeof @onOut == "function"
          @onOut()
      super args
    _addListeners: () ->
      this.graph.element.addEventListener 'click', (e) =>
        if @hoveredSeries
          if typeof @onClick == "function"
            @onClick @hoveredSeries
      super()

  @humanReadableBytes: (bytes) ->
    prefixes = ["bytes", "kB", "MB", "GB", "TB", "PB"]
    exponent = Math.floor(Math.log(bytes) / Math.log(1024))
    return (bytes / Math.pow(1024, Math.floor(exponent))).toFixed(2) + " " + prefixes[exponent]

