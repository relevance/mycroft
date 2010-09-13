(ns zap.html
  (:use [hiccup.core :only [html]]
        [hiccup.page-helpers :only [doctype include-js include-css]]))

(defn layout []
  (html
   (doctype :html5)
   [:head
    [:title "jQTouch &beta;"]
    (include-js "/jqtouch/jquery.1.3.2.min.js"
                "/jqtouch/jqtouch.min.js"
                "/javascripts/minib.js")
    (include-css "/jqtouch/jqtouch.min.css"
                 "/jqtouch/themes/jqt/theme.min.css")]
   [:body
    [:div {:id "placeholder" :class "current"}
     [:ul
      [:li "loading..."]]]
    [:div {:id "content"}]]))

(defn minib-layout [& body]
  (html
    [:head
     [:title "Mini-Browser"]
     (include-css "/stylesheets/shCore.css"
                  "/stylesheets/shThemeDefault.css"
                  "/stylesheets/application.css")
     (include-js "/javascripts/jquery.3.2.min.js"
                 "/javascripts/application.js"
                 "/javascripts/shCore.js"
                 "/javascripts/shBrushClojure.js")]
    [:body {:id "browser"}
     [:div {:id "header"}
      [:h2 "Mini-Browser"]]
     [:div {:id "content"}
      body]
     [:div {:id "footer"}
      "Clojure Mini-Browser"]]))

