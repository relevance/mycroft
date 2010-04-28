(ns zap.main
  (:use compojure clojure.contrib.json.write)
  (:require
   [clojure.contrib.jmx :as jmx]
   [clojure.contrib.str-utils2 :as str]
   [clojure.contrib.repl-utils :as repl]
   [clojure.contrib.seq-utils :only (indexe)]))

(defn keyed
  [expr]
  (if (map? expr)
    expr
    (indexed expr)))

(defprotocol IdComponent
  (as-id [this]))

(extend-protocol IdComponent
  clojure.lang.Keyword
  (as-id [this] (name this))

  java.lang.Object
  (as-id [this] (-> (str this) (str/replace #"[^A-Za-z0-9]" "-"))))

(defn make-id
  [path]
  (str/join "--" (map as-id path)))

(defn gui-item
  [path item]
  (cond
   (associative? item)
   [:a {:href (str "#" (make-id path))} (last path)]
   :else item))

(use 'clojure.contrib.pprint)
(defn gui-seq
  ([expr]
     (gui-seq ["top"] expr))
  ([path expr]
     (cond
      (associative? expr)
      (cons
       [:div {:id (make-id path)}
        [:ul 
         (map
          (fn [[i e]] [:li (gui-item (conj path i) e)])
          (keyed expr))]]
       (->> (keyed expr)
            (filter (fn [[_ e]] (associative? e)))
            (mapcat (fn [[i e]] (gui-seq (conj path i) e)))))
      :else expr)))

(defn beandump []
  (into
   {}
   (map
    #(let [n (.getCanonicalName %)]
       [n (jmx/mbean n)])
    (jmx/mbean-names "*:*"))))

(defroutes browser-routes
  (GET
   "/"
   (serve-file "mobile.html"))
  (GET
   "/beans"
   {:body (json-str (map #(.getCanonicalName %) (jmx/mbean-names "*:*")))
    :headers {"Content-Type" "text/json"}})
  (GET
   "/stuff"
   (html (gui-seq (beandump)))))

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
