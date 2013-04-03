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

  getMax: ->
    values = @get('values')
    prevValue = @get('prevValue')
    max = 0
    if values.length then max = _(values).max((v) -> v.v).v else max = 1.0
    if(prevValue > max) then max = prevValue
    max

  getMin: ->
    values = @get('values')
    prevValue = @get('prevValue')
    min = 0
    if values.length then min = _(values).min((v) -> v.v).v else min = 0.0
    if(prevValue < min) then min = prevValue
    min

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

