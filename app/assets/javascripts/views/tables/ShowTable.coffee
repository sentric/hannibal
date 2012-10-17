class @ShowTableView extends Backbone.View

  initialize: ->
    @palette = new Rickshaw.Color.Palette( { scheme: 'munin' } )
    @tableRegionsChartView = @createTableRegionsChartView($(".table-regions-chart-view"))

  createTableRegionsChartView: ($el) ->
    tableName = $el.data("table-name")
    regions = Regions.byTable(tableName)
    tableRegionsChartView = new TableRegionsChartView
      el: $el
      tableName: tableName
      palette: @palette
      collection: regions
    regions.fetch()
    tableRegionsChartView