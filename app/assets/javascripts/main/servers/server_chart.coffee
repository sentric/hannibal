# Copyright 2012 Sentric. See LICENSE for details.

$ ->
    regions = window.regions = new Regions()
    serverChart = window.serverChart = new ServerChartView
      el: $("#server_chart")
      collection: regions

    regions.fetch()