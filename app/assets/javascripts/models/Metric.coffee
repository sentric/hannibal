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

  findByNames: (names) ->
    @select (metric) -> _(names).contains(metric.getName())

  findByName: (name) -> @findByNames([name])[0]