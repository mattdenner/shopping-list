requirejs.config({
  paths: {
    "jquery":     "http://code.jquery.com/jquery-latest",
    "bootstrap":  "bootstrap.min",
    "mustache":   "mustache-0.7.1"
  },
  shim: {
    "bootstrap": { deps: ["jquery"], exports: "jquery" }
  }
});

require([
  "bootstrap",
  "recipe-to-shopping",
  "page-navigation"
], function(bootstrap, recipe, navigation) {
  jQuery(document).ready(function() {
    navigation.attach(jQuery(".navbar .nav a"), jQuery(".page"));
  });

  jQuery(document).ready(function() {
    var button = jQuery(".generate");
    var source = jQuery("#" + button.attr("data-source"));
    var target = jQuery("#" + button.attr("data-target"));
    recipe.attach(button, source, target);
  });
});
