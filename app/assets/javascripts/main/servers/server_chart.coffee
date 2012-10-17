$ ->
    regions = window.regions = new Regions()
    serverChart = window.serverChart = new ServerChartView
      el: $("#server_chart")
      collection: regions

    regions.fetch()