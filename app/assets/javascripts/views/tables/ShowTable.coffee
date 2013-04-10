# Copyright 2013 Sentric. See LICENSE for details.

class @ShowTableView extends Backbone.View

  initialize: ->
    @palette = new RickshawUtil.TablePalette()
    @tableRegionsChartView = @createTableRegionsChartView($(".table-regions-chart-view"))

    @visualCountDown = new VisualCountDownView
      el: @$(".refresh-text")
      pattern: "(next refresh in %delay% seconds)"
    @visualCountDown.on "done", _.bind(@updateTableRegions, @)

    @$(".sort-options ul li").click(_.bind(@sortItemClicked, @))

    @updateTableRegions()

  createTableRegionsChartView: ($el) ->
    table = $el.data("table")
    regions = Regions.byTable(table.name)
    tableRegionsChartView = new TableRegionsChartView
      el: $el
      table: table
      palette: @palette
      collection: regions
    tableRegionsChartView

  updateTableRegions: ->
    @tableRegionsChartView.collection.fetch()
    @visualCountDown.startCountDown(30, 1, 1000)

  sortItemClicked: (evt) ->
    $element = $(evt.target);
    sort = $element.data('sort')
    @tableRegionsChartView.collection.setSort(sort)
    @tableRegionsChartView.collection.sort()
    @$(".sort-options ul li").each(() ->
      $this = $(@)
      if($this.data('sort') == sort)
        $this.addClass('checked')
      else
        $this.removeClass('checked')
    )

