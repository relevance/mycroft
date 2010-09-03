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
