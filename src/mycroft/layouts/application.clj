(ns mycroft.layouts.application
  (:use [clojure.string :only (split)]
        [clojure.tools.logging :only (info)]
        [hiccup.core :only (html)]
        [hiccup.page-helpers :only (include-css include-js)])
  (:require [mycroft.namespace :as namespace]
            [mycroft.class :as class]
            [mycroft.data :as data]))

(defn minib-layout [title & body]
  (html
   [:head
    [:title title]
    (include-css "/stylesheets/shCore.css"
                 "/stylesheets/shThemeDefault.css")
    [:link {:type "text/css", :href "/stylesheets/mobile.css", :rel "stylesheet", :media ""}]
    [:meta {:name "viewport" :content "user-scalable=no, width=device-width"}]
    (include-js "/javascripts/jquery.1.3.2.min.js"
                "/javascripts/application.js"
                "/javascripts/shCore.js"
                "/javascripts/shBrushClojure.js")]
   [:body {:id "browser"}
    [:h2 {:class "logo"} [:a {:href "/" :class "home"} "Home"] [:a {:href "/index.html"} "Mycroft, a Clojure inspector"]]
    [:div {:id "content"} body]
    [:div {:id "footer"} "Clojure Mini-Browser"]]))

(defn namespaces []
  (minib-layout "Namespaces" (namespace/browser)))

(defn classes [params query-params]
  (info "params" params)
  (info "query-params" query-params)
  (let [classname (:* params)
        cls (Class/forName classname)]
    (minib-layout classname
                  (class/render cls query-params cls))))

(defn vars [params query-params]
  (info "params" params)
  (info "query-params" query-params)
  (let [qname (:* params)
        [ns var] (split qname #"/")]
    (namespace/safe-load-ns ns)
    (minib-layout qname
                  (if var
                    (data/render (find-var (symbol qname)) query-params)
                    (namespace/var-browser ns)))))