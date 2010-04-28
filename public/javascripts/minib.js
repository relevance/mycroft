    var jQT = new $.jQTouch({
        icon: 'jqtouch.png',
        addGlossToIcon: false,
        startupScreen: 'jqt_startup.png',
        statusBar: 'black',
        preloadImages: [
            '/jqtouch/themes/jqt/img/back_button.png',
            '/jqtouch/themes/jqt/img/back_button_clicked.png',
        '/jqtouch/themes/jqt/img/button_clicked.png',
            '/jqtouch/themes/jqt/img/grayButton.png',
            '/jqtouch/themes/jqt/img/whiteButton.png',
            '/jqtouch/themes/jqt/img/loading.gif'
        ]
    });

    function fitb(template, data) {
        return template.replace("loading...", data);
    }
    function fim(selector, data) {
        var template = $(selector).html();
        $(selector).html($.map(data, function(item) {return fitb(template, item);}).join(""));
    }
    function ff(re, stringable) {
        var m = String(stringable).match(re);
        if (m) 
            return m[1];
        else 
            return null;
    }
    function ns() {
        return ff(/m\/([^/]*)\//, document.location);
    }

$(function () {
//     $.getJSON("/beans", null, function(e) {
//         fim("#list", e);
//     });
  // $("#content").load("/stuff", null, function() {
  //   jQT.goTo("#top");
  // });
  $.get("/stuff", null, function(data) {
    $("#content").replaceWith(data);
    jQT.goTo("#top");
  });
});
