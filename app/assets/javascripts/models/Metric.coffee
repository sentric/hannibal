# Copyright 2012 Sentric. See LICENSE for details.

class @Metric extends Backbone.Model
  url: ->
    Routes.Metric.showJson
      target: @get('target')
      name: @getName()

  getName: ->
    @get('name')

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

  getSeriesValues: ->
    step = Math.round(@getStep() / 1000)
    begin = Math.round(@getBegin() / 1000)
    end = Math.round(@getEnd() / 1000)
    values = @getValues()
    pointIndex = -1
    pointValue = @getPrevValue()

    _.range(begin, end + step, step).map((ts) =>
      if(pointIndex < values.length - 1 && ts > Math.round(values[pointIndex+1].ts / 1000))
        pointIndex = pointIndex + 1
        pointValue = values[pointIndex].v
      return {
        x: ts
        y: pointValue
      }
    );


class @Metrics extends Backbone.Collection
  model: Metric

  @byNames: (target, names) ->
    metrics = new Metrics([])
    metrics.url = Routes.Metrics.listJson
      target: target
      metric: names
    metrics

  isEmpty: ->
    @all(metric -> metric.isEmpty())


class @MetricGroup
  constructor: (name, metrics) ->
    @name = name
    @metrics = metrics

  getName: ->
    @name

  getStep: ->
    60000

  getBegin: ->
    @metrics[0].getBegin()

  getEnd: ->
    @metrics[0].getBegin()

  isEmpty: ->
    _(@metrics).all(m -> m.isEmpty())

  getMax: ->
    _(@metrics).max(m -> m.getMax())

  getMin: ->
    _(@metrics).min(m -> m.getMin())

  getSeriesValues: ->
    step = Math.round(@getStep() / 1000)
    begin = Math.round(@getBegin() / 1000)
    end = Math.round(@getEnd() / 1000)
    emptyResult = _.range(begin, end + step, step).map((ts) => {x: ts, y: 0})

    _(@metrics)
      .map(m -> m.getSeriesValues())
      .reduce(((memo, seriesValues) ->
        memo.map((val, idx) ->
          {x: val.x, y: val.y + seriesValues[idx].y}
        )
      ), emptyResult)
