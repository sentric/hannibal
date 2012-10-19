# Copyright 2012 Sentric. See LICENSE for details.

class @MetricSeries

  constructor: (metric, color) ->
    @name = metric.getHumanReadableName()
    @color = color
    @metric = metric

    step = Math.round(metric.getStep() / 1000)
    values = metric.getValues()
    begin = Math.round(metric.getBegin() / 1000)
    end = Math.round(metric.getEnd() / 1000)
    pointIndex = -1
    pointValue = metric.getPrevValue()

    if values.length then @min = _(values).min((v) -> v.v).v else @min = 0.0
    if(pointValue < @min) then @min = pointValue
    if values.length then @max = _(values).max((v) -> v.v).v else @max = 1.0
    if(pointValue > @max) then @max = pointValue
    @mm = @max - @min
    if @mm == 0.0
      console.log ("min-max difference is 0, setting to 1.0")
      @mm = 1.0

    @data = _.range(begin, end + step, step).map((ts) =>
      if(pointIndex < values.length - 1 &&
         ts > Math.round(values[pointIndex+1].ts / 1000))
        pointIndex = pointIndex + 1
        pointValue = values[pointIndex].v

      return {
        x: ts
        y: @normalize(pointValue)
      }
    );

  denormalize: (v) ->
    Math.round((v - 0.025) * @mm + @min)

  normalize: (v) ->
    (v - @min) / @mm + 0.025