(ns mycroft.jmx
  (:require [clojure.java.jmx :as jmx]))

(defn beans
  "Returns a sorted map of bean-name -> bean for all JMX beans
   matching name."
  [name]
  (into
   (sorted-map)
   (map
    #(let [n (.getCanonicalName %)]
       [n (jmx/mbean n)])
    (jmx/mbean-names name))))


