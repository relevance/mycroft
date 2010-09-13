(ns zap.jmx
  (:require [clojure.contrib.jmx :as jmx]))

(defn beans
  [name]
  (into
   (sorted-map)
   (map
    #(let [n (.getCanonicalName %)]
       [n (jmx/mbean n)])
    (jmx/mbean-names name))))


