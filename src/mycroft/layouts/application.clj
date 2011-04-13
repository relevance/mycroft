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

(defn read-param-string
  "Use Clojure reader to read a param strings, or return
   default is param string empty/nil."
  ([s] (read-param-string s nil))
  ([s default]
     (if (seq s)
       (read-string s)
       default)))

(defn normalize-params
  [params]
  (-> params
      (update-in [:selectors] read-param-string)
      (update-in [:headers] read-param-string)
      (update-in [:start] read-param-string 0)))

(defn classes [params]
  (let [classname (:* params)
        cls (Class/forName classname)]
    (minib-layout classname
                  (class/render cls (normalize-params params) cls))))

(defn vars [params]
  (let [qname (:* params)
        [ns var] (split qname #"/")]
    (namespace/safe-load-ns ns)
    (minib-layout qname
                  (if var
                    (data/render (find-var (symbol qname)) (normalize-params params))
                    (namespace/var-browser ns)))))