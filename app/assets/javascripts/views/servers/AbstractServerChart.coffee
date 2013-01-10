# Copyright 2012 Sentric. See LICENSE for details.

root = exports ? @

class @AbstractServerChartView extends Backbone.View
  klass = @

  @chartContent = """
    <div class="y-axis"></div>
    <div class="chart"></div>
    <div class="x-axis"></div>
    <div class="legend"></div>
    <div class="stack-toggle"></div>
  """

  initialize: ->
    @series = []
    @tableColors = {}
    @palette = new RickshawUtil.TablePalette()

    @updateAggregates()
    @collection.on "add remove reset change", _.bind(@updateAggregates, @)

  updateAggregates: ->
    @hostNames = _.keys(@collection.groupByAttribute("serverHostName")).sort()
    @regionsByTable = @collection.groupByAttribute("tableName")
    @hostNameMap = _.reduce(@hostNames, ((indexes, hostName, i)-> indexes[hostName] = i; indexes), {})

  getChartSeries: -> throw "getChartSeries() not implemented in #{klass.name}"

  createGraphComponents: ->
    graph = new Rickshaw.Graph
      element: @$('.chart').get(0)
      renderer: 'bar'
      series: @series

    legend = new Rickshaw.Graph.Legend
      element: @$('.legend').get(0)
      graph: graph

    highlighter = new Rickshaw.Graph.Behavior.Series.Highlight
      graph: graph
      legend: legend

    shelving = new Rickshaw.Graph.Behavior.Series.Toggle
      graph: graph
      legend: legend

    return {graph, legend, highlighter, shelving}

  getColor: (tableName) -> 
    @palette.color(tableName)

  hostNameAtIndex: (i) =>
    @hostNames[i]

  render: ->
    RickshawUtil.mergeSeriesData(@getChartSeries(), @series)

    if(!@graphComponents)
      @$el.html(klass.chartContent)
      @graphComponents = @createGraphComponents()
      @graphComponents.graph.render()
      @legendShortener = new RickshawUtil.LegendShortener
        length: 15
        el: @$(".legend")
      @restoreLegendState()
    else
      @graphComponents.graph.update()
      @graphComponents.graph.render()

  storeLegendState: () ->
    RickshawUtil.LegendHelper.storeState(@graphComponents.legend, "cluster.legend.disabledTables")

  restoreLegendState: () ->
    RickshawUtil.LegendHelper.restoreState(@graphComponents.legend, "cluster.legend.disabledTables")