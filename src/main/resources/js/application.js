requirejs.config({
  paths: {
    "jquery":    "http://code.jquery.com/jquery-latest",
    "bootstrap": "bootstrap.min"
  },
  shim: {
    "bootstrap": { deps: ["jquery"], exports: "jquery" }
  }
});

require([
  "jquery",
  "mustache-0.7.1",
  "statemachine.min",
  "text!/templates/item.html",

  // Past this point we do not care about the module as it isn't AMD!
  "underscore-1.4.3",
  "bootstrap"
], function(jQuery, Mustache, Statemachine, groupTemplate) {
  // Deals with rendering an individual group using the Mustache template
  var groupRenderer = function(items, groupName) {
    return jQuery(Mustache.render(groupTemplate, { name: groupName, items: items }));
  }

  var clickHandler = function(target) {
    var container = target.parents(".item");
    var button    = container.find(".btn");

    // Small helper for generating callbacks in the statemachine that will deal with changing
    // the button
    var switchButton = function(name) {
      var buttonCss = function(name) { return "btn-" + name; }
      var classes = _.map(['action','danger','warning','success','info','inverse'], buttonCss).join(" ");
      return function(event, from, to) { button.removeClass(classes).addClass(buttonCss(name)); };
    };
    var switchAction = function(text, action) {
      return function(event, from, to) { button.first().text(text).attr('data-action', action); };
    };
    var switchRow = function(state) {
      return function(event, from, to) { container.addClass(state); };
    };
    var resetRow = function(state) {
      return function(event, from, to) { container.removeClass(state); };
    };
    var callbacks = function(callbacksToExecute) {
      return function(event, from, to) {
        _.each(callbacksToExecute, function(callback) { callback(event, from, to); });
      };
    }

    // All items behave like a statemachine, in that they are initially "needed", and can
    // then be "bought", already "have", or are "unavailable" when shopping.  When someone
    // clicks on an action button it triggers an event on the FSM which causes something
    // to happen (like the text of the button to change).
    var statemachine = Statemachine.create({
      initial: container.attr('data-state'),
      events: [
        { name: 'buy',         from: ['needed','unavailable'], to: 'bought' },
        { name: 'have',        from: 'needed',                 to: 'have' },
        { name: 'unavailable', from: 'needed',                 to: 'unavailable' },
        { name: 'return',      from: 'bought',                 to: 'needed' },
        { name: 'need',        from: 'have',                   to: 'needed' }
      ],
      callbacks: {
        // The button state changes based on the transitions that occur.
        onenterneeded:      callbacks([ switchButton("success"), switchAction("Buy", "buy") ]),
        onenterbought:      callbacks([ switchButton("danger"),  switchAction("Return", "return"), switchRow("success") ]),
        onleavebought:      callbacks([ resetRow("success") ]),
        onenterunavailable: callbacks([ switchButton("warning"), switchAction("Buy", "buy") ]),
        onenterhave:        callbacks([ switchButton("inverse"), switchAction("Mistake!", "need"), switchRow("success") ]),
        onleavehave:        callbacks([ resetRow("success") ]),

        // Regardless of the transition we always set the appropriate state on the item.
        // NOTE: using 'callbacks' here to make it easier to extend should I need to!
        onchangestate: callbacks([
          function(event, from, to) { container.attr("data-state", to); }
        ])
      }
    });
    statemachine[target.attr('data-action')]();
  };

  jQuery(document).ready(function() {
    // When you click on an action we need to mark what has happened!
    jQuery("#shopping-list").delegate(".item .action a", "click", function() {
      clickHandler(jQuery(this));
    });

    jQuery(".generate").bind("click", function() {
      var source = jQuery("#" + jQuery(this).attr("data-source"));
      var target = jQuery("#" + jQuery(this).attr("data-target"));

      jQuery.ajax({
        type: "POST",
        url: "/",
        headers: {
          "Accepts": "application/json",
          "Content-Type": "text/plain"
        },

        data: source.val(),

        success: function(data) {
          var groups = _.groupBy(
            _.map(data, function(value, index) { return _.defaults(value, {index: index}); }),
            function(value) { return value.category || "Unknown"; }
          );

          var unknown = _.pick(groups, "Unknown")["Unknown"] || [];

          target.empty();
          if (unknown.length > 0) { target.append(groupRenderer(unknown, "Unknown")); }
          target.append(_.map(_.omit(groups, "Unknown"), groupRenderer));
        }
      });
    });
  });
});
