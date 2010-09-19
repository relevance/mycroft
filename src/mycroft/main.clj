(ns mycroft.main
  (:use [ring.adapter.jetty :only (run-jetty)]
        [ring.util.response :only (redirect)]
        [compojure.core :only (defroutes GET POST routes)]
        [hiccup.core :only (html)]
        [hiccup.page-helpers :only [include-js include-css]]
        [clojure.pprint :only (pprint)]
        [clojure.walk :only (keywordize-keys)])
  (:require
   [compojure.route :as route]
   [mycroft.jmx :as jmx]
   [mycroft.data :as data]
   [mycroft.jqtouch :as jqtouch]
   [mycroft.namespace :as namespace]
   [mycroft.docs :as docs]
   [mycroft.history :as history]
   [clojure.string :as str]))

(defn minib-layout [title & body]
  (html
    [:head
     [:title title]
     (include-css "/stylesheets/shCore.css"
                  "/stylesheets/shThemeDefault.css")
                  [:link {:type "text/css", :href "/stylesheets/application.css", :rel "stylesheet", :media "screen (min-width: 600px)"}]
                  [:link {:type "text/css", :href "/stylesheets/mobile.css", :rel "stylesheet", :media "only screen and (max-width: 600px)"}]
     [:meta {:name "viewport" :content "user-scalable=no, width=device-width"}]
     (include-js "/jqtouch/jquery.1.3.2.min.js"
                 "/javascripts/application.js"
                 "/javascripts/shCore.js"
                 "/javascripts/shBrushClojure.js")]
    [:body {:id "browser"}
     [:div {:id "header"}]
     [:div {:id "content"}
      body]
     [:div {:id "footer"}
      "Clojure Mini-Browser"]]))

(defn with-logging [handler]
  (fn [request]
    (let [start (System/nanoTime)
          response (handler request)
          elapsed (/ (double (- (System/nanoTime) start)) 1000000.0)]
      (when response
        (println (str (:uri request) " [" (:request-method request) "] " elapsed " msec"
                      "\n\tParameters " (:params request)
                      "\n\tSession " (:session request)))
        response))))

(defroutes jmx-routes
  (GET "/jmx" [] (jqtouch/layout))
  (GET "/stuff" [] (html (jqtouch/gui-seq (jmx/beans "*:*")))))

(defroutes namespace-routes
  (GET "/vars" []
       (html
        (minib-layout
         "Namespaces"
         (namespace/browser)))))

(defroutes var-routes
  (GET
   "/vars/*"
   {:keys [params query-params]}
   (let [qname (get params "*")
         [ns var] (str/split qname #"/")]
     (namespace/safe-load-ns ns)
     (html
      (minib-layout
       qname
       (if var
         (data/render (find-var (symbol qname)) (keywordize-keys query-params))
         (namespace/var-browser ns)))))))

(def dynamic-routes (routes jmx-routes
                            (-> namespace-routes history/with-recent-history)
                            var-routes))

(defroutes static-routes
  (route/files "/")
  (route/not-found "not found"))

(defroutes app
  (routes (-> dynamic-routes with-logging)
          static-routes))

(defn -main []
  (run-jetty (var app) {:port 8080
                        :join? false}))
