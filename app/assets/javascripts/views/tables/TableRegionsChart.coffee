class @TableRegionsChartView extends Backbone.View

  initialize: ->
    @tableName = @options.tableName
    @palette = @options.palette
    @collection.on "reset", _.bind(@render, @)

  render: ->
    console.log("render", @collection)

    if @collection.isZeroLength()
      regions = @collection.reduce((memo, region) ->
        "#{memo}<li><a href=\"#{Routes.Regions.show({name:region.get('regionName')})}\">#{region.get('regionName')}<a/></li>"
      , "" )
      @$el.html("The table #{@tableName} seems to have not any data yet, it contains the regions:<ul>#{regions}</ul>")
    else
      @series = [@createSeries(@tableName, @collection)]

      @graph =  new Rickshaw.Graph
        element: @$(".chart")[0],
        renderer: 'bar',
        series: @series

      @yAxis = new Rickshaw.Graph.Axis.Y
        graph: @graph,
        orientation: 'left',
        element: @$(".y-axis")[0]
        tickFormat: (val) -> if val > 0 then RickshawUtil.humanReadableBytes(val * 1024 * 1024) else 0

      @xAxis = new RickshawUtil.LeftAlignedXAxis
        graph: @graph,
        element: @$(".x-axis")[0]
        tickFormat: (x) => if x < @collection.length then "##{x+1}" else ""

      @hoverDetail = new RickshawUtil.InteractiveHoverDetail
        graph: @graph
        xFormatter: ((x) =>
          region = @collection.at(x)
          "##{x+1} : #{region.get('regionName')}"
        )
        yFormatter: ((y) => y)
        formatter: ((series, x, y, formattedX, formattedY, d) =>
          region = @collection.at(x)
          headline = "<b>" + region.get('startKey') + "</b>"
          host = "Host:&nbsp;" + region.get('serverHostName')
          storefileSize = "Size (MB):&nbsp;" + region.get("storefileSizeMB").toFixed(0)
          storefiles = "Storefiles:&nbsp;" + region.get("storefiles")
          headline + "<br>" + host + "<br>" + storefileSize + "<br>" + storefiles
        )
        onClick: (series) =>
          document.location.href = Routes.Regions.show
            name: series.value.region

      @graph.render()

  createSeries: (name, regions) ->
    minSize = regions.max((region) -> region.get('storefileSizeMB')).get('storefileSizeMB') / 100
    {
      data: regions.map((region, x) ->
        y = region.get('storefileSizeMB')
        return {
          x: x
          y: if y > minSize then y else minSize
          region: region.get('regionName')
        }
      ),
      color: @palette.color()
    }