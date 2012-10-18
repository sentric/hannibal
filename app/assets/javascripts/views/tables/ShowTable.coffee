class @ShowTableView extends Backbone.View

  initialize: ->
    @palette = new Rickshaw.Color.Palette( { scheme: 'munin' } )
    @tableRegionsChartView = @createTableRegionsChartView($(".table-regions-chart-view"))

  createTableRegionsChartView: ($el) ->
    table = $el.data("table")
    regions = Regions.byTable(table.name)
    tableRegionsChartView = new TableRegionsChartView
      el: $el
      table: table
      palette: @palette
      collection: regions
    regions.fetch()
    tableRegionsChartView