class @VisualCountDownView extends Backbone.View
  initialize: ->
    @pattern = @options.pattern || "(Next Refresh in %delay%ms)"

  startCountDown: (length, updateInterval, multiplicator = 1) ->
    @remaining = length
    @interval = window.setInterval(=>
      @remaining = @remaining - updateInterval
      if @remaining <= 0
        window.clearInterval @interval
        @trigger "done"
        $(@el).html("")
      else
        $(@el).html(@pattern.replace("%delay%", @remaining))
    , updateInterval * multiplicator )
