# Copyright 2012 Sentric. See LICENSE for details.

class StackingToggleView extends Backbone.View
  events:
    "click .stacking-options .enable": -> @toggle(true)
    "click .stacking-options .disable": -> @toggle(false)

  initialize: ->
    @graph = @options.graph
    @enabled = !@graph.renderer.unstack

    @render()

  toggle: (flag) ->
    @enabled = flag
    @graph.renderer.unstack = !flag
    @graph.update()
    @render()

  render: ->
    @$el.html """
      <ul class="stacking-options">
        <li class="enable #{if @enabled then "checked" else ""}">Stacked</li>
        <li class="disable #{if !@enabled then "checked" else ""}">Grouped</li>
      </ul>
    """

class @ServerChartView extends  AbstractServerChartView

  initialize: ->
    super

    @collection.on "reset", _.bind(@render, @)
    @on "table:click", -> document.location.href = Routes.Tables.show
      name: arguments[0].name

  getChartSeries: ->
    hostNames = @hostNames
    hostNameMap = @hostNameMap

    if hostNames.length == 1
      hostNames.push ""
      hostNameMap[""] = 1

    series = for own tableName, regionInfos of @regionsByTable
      groupedByHost = _.groupBy(regionInfos, (regionInfo) -> regionInfo.get("serverHostName"))
      _.each hostNames, (hostName)-> groupedByHost[hostName] = [] unless groupedByHost[hostName]
      values = for own hostName, regionInfos of groupedByHost
        x = hostNameMap[hostName]
        y = _.reduce(regionInfos, ((sum, regionInfo) -> sum + regionInfo.get('storefileSizeMB')), 0)
        {x, y, regionInfos}

      values = _.sortBy(values, (val)-> val.x)
      {} =
        name: tableName
        data: values
        color: @getColor(tableName)


  createGraphComponents: ->
    components = super()
    graph = components.graph

    hostNameCount = @hostNames.length
    xAxisTicks = hostNameCount

    xAxis = new RickshawUtil.LeftAlignedXAxis
      element: @$('.x-axis').get(0)
      graph: graph,
      ticks: xAxisTicks
      tickFormat: @hostNameAtIndex

    yAxis = new Rickshaw.Graph.Axis.Y
      element: @$('.y-axis').get(0)
      graph: graph
      orientation: 'left'
      tickFormat: (val)=> if val > 0 then RickshawUtil.humanReadableBytes(val * 1024 * 1024) else 0

    @hoverDetail = new RickshawUtil.InteractiveHoverDetail
      graph: graph
      xFormatter: @hostNameAtIndex
      yFormatter: (y)-> "#{y} MB",
      formatter: (series, x, y, formattedX, formattedY, d) ->
        size = formattedY
        count = d.value.regionInfos.length
        return """
          <b>#{series.name}</b><br/>
          #{size}<br/>
          #{count} Regions
        """
      onClick: (series) =>
        @trigger("table:click", series)

    stackingToggle = new StackingToggleView
      el: @$('.stack-toggle')
      graph: graph

    allSeriesToggle = new RickshawUtil.AllSeriesToggle
      toggle: components.shelving
      toggleText: "All tables"

    _.extend(components, {xAxis, yAxis, @hoverDetail, stackingToggle, allSeriesToggle})