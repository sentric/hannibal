# Copyright 2012 Sentric. See LICENSE for details.

class @Metric extends Backbone.Model
  url: ->
    Routes.Metric.showJson
      target: @getTarget()
      name: @getName()

  getName: ->
    @get('name')

  getTarget: ->
    @get('target')

  getHumanReadableName: ->
    switch @getName()
      when "storefiles" then "Storefiles"
      when "storefileSizeMB" then "Storefile Size (MB)"
      when "memstoreSizeMB" then "Memstore Size (MB)"
      when "compactions" then "Compactions"

  getStep: ->
    60000

  getBegin: ->
    @get('begin')

  getEnd: ->
    @get('end')

  getValues: ->
    @get('values')

  getPrevValue: ->
    @get('prevValue')

  isEmpty: ->
    @get('isEmpty')

class @Metrics extends Backbone.Collection
  model: Metric

  @byNames: (target, names) ->
    metrics = new Metrics([])
    metrics.url = Routes.Metrics.listJson
      target: target
      metric: names
    metrics

  isEmpty: ->
    @all((metric) -> metric.isEmpty())

  initialize: (options) ->
    @series = []
    @palette = new Rickshaw.Color.Palette( { scheme: [
      '#B1354A', # Storefiles
      '#B12BA0', # Compactions
      '#68B15D', # Memstore Size
      '#4E5FB1', # Storefile Size
      '#56AFB1', # not used
      '#B1A667', # not used
    ] } )

  populateSeries: () ->
    @each((metric) => @findOrCreateSeries(metric.getName()).populate(metric))

  findOrCreateSeries: (name) ->
    found = @findSeries(name)
    if(!found)
      found = new MetricSeries(name, @palette.color())
      @series.push(found)
    found

  findSeries: (name) -> _(@series).find((series) -> series.metricName == name)
