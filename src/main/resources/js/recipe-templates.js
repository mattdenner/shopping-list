define(
  'recipe-templates',
  _.union(
    [ "jquery", "mustache" ],
    _.map(["item", "alert", "progress"], function(n) { return "text!/templates/"+n+".html"; })
  ),
  function(jQuery, Mustache, template1, template2, template3) {
    return {
      group:    groupRenderer,
      alert:    alertRenderer(),
      progress: progressRenderer()
    };

    // Renders an individual group using the mustache template, ensuring that each item in the
    // group has a statemachine attached to it.
    function groupRenderer(attachGroupStatemachine, attachItemStatemachine, items, groupName) {
      var html = jQuery(Mustache.render(template1, { name: groupName, items: items }));

      var groupStatemachine = attachGroupStatemachine(html.first(".group"));
      _.each(html.filter(".item"), function(i) {
        var item = jQuery(i);
        groupStatemachine.connect(
          attachItemStatemachine(item, item.find(".btn"), item.find(".action a"))
        );
      });
      return html;
    }

    function alertRenderer() {
      return Mustache.render(template2);
    }

    function progressRenderer() {
      var html = jQuery(Mustache.render(template3));
      return _.extend(html, {
        nudge: function(percentage) { html.find('.bar').css('width', ""+percentage+"%"); }
      });
    }
  }
);
