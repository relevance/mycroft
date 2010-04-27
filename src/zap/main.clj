(ns zap.main
  (:use compojure clojure.contrib.json.write)
  (:require
   [clojure.contrib.jmx :as jmx]
   [clojure.contrib.str-utils2 :as str]
   [clojure.contrib.repl-utils :as repl]))

(defroutes browser-routes
  (GET
   "/"
   (serve-file "mobile.html"))
  (GET
   "/beans"
   {:body (json-str (map #(.getCanonicalName %) (jmx/mbean-names "*:*")))
    :headers {"Content-Type" "text/json"}}))

(defroutes static-routes
  (GET "/*" (or (serve-file (params :*)) :next))
  (ANY "*" (page-not-found)))

(defroutes app-routes
  (routes browser-routes static-routes))

(defn -main []
  (run-server
   {:port 8080}
   "/*"
   (servlet app-routes)))
