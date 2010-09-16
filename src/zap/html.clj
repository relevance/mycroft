(ns zap.html
  (:use [hiccup.core :only [html]]
        [hiccup.page-helpers :only [include-js include-css]]))

(defn minib-layout [title & body]
  (html
    [:head
     [:title title]
     (include-css "/stylesheets/shCore.css"
                  "/stylesheets/shThemeDefault.css"
                  "/stylesheets/application.css")
     (include-js "/jqtouch/jquery.1.3.2.min.js"
                 "/javascripts/application.js"
                 "/javascripts/shCore.js"
                 "/javascripts/shBrushClojure.js")]
    [:body {:id "browser"}
     [:div {:id "header"}
      [:h2 title]]
     [:div {:id "content"}
      body]
     [:div {:id "footer"}
      "Clojure Mini-Browser"]]))

