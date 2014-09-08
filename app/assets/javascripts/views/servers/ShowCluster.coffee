# Copyright 2014 YMC. See LICENSE for details.

class @ShowClusterView extends Backbone.View

  initialize: ->
    regions = new Regions()
    @serverChart = window.serverChart = new ServerChartView
      el: @$("#server_chart")
      collection: regions

    @visualCountDown = new VisualCountDownView
      el: @$(".server-refresh-text")
      pattern: "(next refresh in %delay% seconds)"
    @visualCountDown.on "done", _.bind(@updateClusterRegions, @)

    @updateClusterRegions()

  updateClusterRegions: ->
    @serverChart.collection.fetch()
    @visualCountDown.startCountDown(30, 1, 1000)

