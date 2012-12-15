define(
  "page-navigation",
  [ "jquery", "statemachine.min", "underscore-1.4.3" ],
  function(jQuery, Statemachine) {
    return { attach: create };

    function create(links, pages) {
      var states = _.map(links, function(l) { return jQuery(l).attr("data-page"); });

      // Helper to adjust the active navigation item more simply
      var moveActive = function(operation, target) {
        links.filter("[data-page="+target+"]").parent()[operation+"Class"]("active");
        pages.filter("#"+target)[operation+"Class"]("active");
      }

      // Here's the statemachine that will maintain the active tab in the navigation, as
      // well as ensuring that the appropriate page is also active.
      var statemachine = Statemachine.create({
        initial: links.first(".active").attr("data-page"),
        events: _.map(states, function(state) {
          var otherStates = _.reject(states, function(n) { return n == state; });
          return { name: state, from: otherStates, to: state };
        }),
        callbacks: {
          onchangestate: function(event, from, to) {
            moveActive("remove", from);
            moveActive("add",    to);
          }
        }
      });

      links.click(function() { statemachine[jQuery(this).attr('data-page')](); });

      return statemachine;
    }
  }
)
