define(
  'recipe-to-shopping',
  [
    "jquery", "statemachine.min",
    "recipe-templates"
  ],
  function(jQuery, Statemachine, templates) {
    return { attach: create };

    function groupRenderer(items, groupName) {
      return templates.group(attachGroupStatemachine, attachItemStatemachine, items, groupName);
    }

    function create(button, source, target) {
      button.click(function() {
        jQuery.ajax({
          type: "POST",
          url: "/",
          headers: {
            "Accepts": "application/json",
            "Content-Type": "text/plain"
          },

          data: source.val(),

          beforeSend: function() {
            source.parent().before(templates.progress);
            templates.progress.nudge(10);
          },
          complete: function() {
            templates.progress.nudge(100);
            templates.progress.remove();
          },

          success: function(data) {
            templates.progress.nudge(60);

            var groups = _.groupBy(
              _.map(data, function(value, index) { return _.defaults(value, { id: index }); }),
              function(value) { return value.category || "Unknown"; }
            );
            var unknown = _.pick(groups, "Unknown")["Unknown"] || [];

            target.empty();
            if (unknown.length > 0) { target.append(groupRenderer(unknown, "Unknown")); }
            target.append(_.map(_.omit(groups, "Unknown"), groupRenderer));

            source.parent().before(templates.alert);
          }
        });
      });
    }

    function attachItemStatemachine(container, button, targets) {
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
          onenterbought:      callbacks([ switchButton("danger"),  switchAction("Return", "return"), switchContainer(container, "success") ]),
          onleavebought:      callbacks([ resetContainer(container, "success") ]),
          onenterunavailable: callbacks([ switchButton("warning"), switchAction("Buy", "buy") ]),
          onenterhave:        callbacks([ switchButton("inverse"), switchAction("Mistake!", "need"), switchContainer(container, "success") ]),
          onleavehave:        callbacks([ resetContainer(container, "success") ]),

          // Regardless of the transition we always set the appropriate state on the item.
          // NOTE: using 'callbacks' here to make it easier to extend should I need to!
          onchangestate: callbacks([
            function(event, from, to) { container.attr("data-state", to); }
          ])
        }
      });
      targets.click(function() {
        statemachine[jQuery(this).attr('data-action')]();
      });
      return statemachine;
    }

    function attachGroupStatemachine(container) {
      var adjustCounter = function(name, adjustment) {
        var badge = container.find(".badges ."+name);
        var count = adjustment(parseInt(badge.text()));
        badge.text(count);
        return count;
      };

      var checkCounter = function(name) {
        return parseInt(container.find(".badges ."+name).text());
      };

      var countOfItemsInGroup = 0;
      var hasCompleted = function() {
        return _.reduce(
          _.map(["bought","have"],checkCounter),
          function(memo, count) { return memo+count; },
          0
        ) == countOfItemsInGroup;
      };

      var statemachine = Statemachine.create({
        initial: 'incomplete',
        events: [
          { name: 'completed',  from: 'incomplete',               to: 'complete' },
          { name: 'incomplete', from: ['incomplete', 'complete'], to: 'incomplete' }
        ],
        callbacks: {
          onentercomplete: callbacks([ switchContainer(container, "success") ]),
          onleavecomplete: callbacks([ resetContainer(container) ])
        }
      });

      return _.defaults(statemachine, {
        itemChangeStateHandler: function(event, from, to) {
          adjustCounter(from, function(count) { return count-1; });
          adjustCounter(to,   function(count) { return count+1; });
          statemachine[hasCompleted() ? 'completed' : 'incomplete']();
        },
        connect: function(statemachine) {
          statemachine.onchangestate.push(this.itemChangeStateHandler);
          adjustCounter("needed", function() { return ++countOfItemsInGroup; });
        }
      });
    }

    // Functions to help with dealing with the statemachine and the state of the HTML
    function callbacks(callbacksToExecute) {
      return _.defaults(function(event, from, to) {
        _.each(callbacksToExecute, function(callback) { callback(event, from, to); });
      }, {
        push: function(callback) {
          callbacksToExecute.push(callback);
        }
      });
    }
    function switchContainer(container, state) {
      return function(event, from, to) { container.addClass(state); };
    }
    function resetContainer(container, state) {
      return function(event, from, to) { container.removeClass(state); };
    }
  }
)
