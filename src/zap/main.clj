(ns zap.main
  (:use [ring.adapter.jetty :only (run-jetty)]
        [ring.util.response :only (redirect)]
        [compojure.core :only (defroutes GET POST routes)]
        [hiccup.core :only (html)]
        [zap.html :only [minib-layout]]
        [clojure.pprint :only (pprint)]
        [clojure.walk :only (keywordize-keys)])
  (:require
   [compojure.route :as route]
   [zap.jmx :as jmx]
   [zap.data :as data]
   [zap.jqtouch :as jqtouch]
   [zap.namespace :as namespace]
   [zap.docs :as docs]
   [zap.history :as history]
   [clojure.string :as str]))

(defn var-detail
  [ns var params]
  (when var
    (let [sym (namespace/var-symbol ns var)
          var (find-var sym)]
      (if (fn? @var)
        (html (docs/render var sym))
        (html (data/render var (keywordize-keys params)))))))

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
     (html
      (minib-layout
       (if var (str "Var: " qname) (str "Namespace: " ns))
       (if var
         (var-detail ns var query-params)
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
