# Copyright 2013 Sentric. See LICENSE for details.

class @MetricsSeries

  constructor: (doNormalize = true, palette = null) ->
    @doNormalize = doNormalize
    @series = []
    @palette = palette
    if !@palette
      @palette = new Rickshaw.Color.Palette( { scheme: [
        '#B1354A',
        '#B12BA0',
        '#68B15D',
        '#4E5FB1',
        '#B1A667',
        '#56AFB1', # not used
      ] } )

  populate: (metrics) ->
    _(metrics).each((metric) => @findOrCreateSeries(metric.getName()).populate(metric))

  findOrCreateSeries: (name) ->
    found = @findSeries(name)
    if(!found)
      found = new MetricSeries(name, @palette.color(name), @doNormalize)
      @series.push(found)
    found

  findSeries: (name) -> _(@series).find((series) -> series.metricName == name)


class @MetricSeries

  constructor: (metricName, color, doNormalize = true) ->
    @metricName = metricName
    @color = color
    @max = -1;
    @min = 99999999;
    @doNormalize = doNormalize

  populate: (metric) ->
    @name = @getHumanReadableName()
    @unit = @getHumanReadableUnit()
    @metric = metric
    @min = metric.getMin()
    @max = metric.getMax()

    @mm = @max - @min
    if @mm == 0.0
      console.log ("min-max difference is 0, setting to 1.0")
      @mm = 1.0

    values = metric.getSeriesValues()
    @data = _(values).map((v) =>
      {
        x: v.x,
        y: @normalize(v.y)
      }
    );

  denormalize: (v) ->
    if @doNormalize
      Math.round((v - 0.025) * @mm + @min)
    else
      v

  normalize: (v) ->
    if @doNormalize
      (v - @min) / @mm + 0.025
    else
      v

  getHumanReadableName: ->
    switch @metricName
      when "storefiles" then "Storefiles"
      when "storefileSizeMB" then "Storefile Size"
      when "memstoreSizeMB" then "Memstore Size"
      when "compactions" then "Compactions"
      else @metricName

  getHumanReadableUnit: ->
    switch @metricName
      when "storefiles" then ""
      when "storefileSizeMB" then "MB"
      when "memstoreSizeMB" then "MB"
      when "compactions" then ""
      else "Compactions"
