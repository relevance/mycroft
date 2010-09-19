(ns mycroft.examples.xml
  (:require [clojure.xml :as xml])
  (:import javax.xml.parsers.DocumentBuilderFactory))


(defn resource-name
  [r]
  (.getPath (.getResource (.getContextClassLoader (Thread/currentThread)) r)))

(defn dom
  [r]
  (-> (DocumentBuilderFactory/newInstance)
      (.newDocumentBuilder)
      (.parse r)))

(def xml-example
  (let [path (resource-name "compositions.xml")
        raw (slurp path)]
    {:raw-text raw
     :as-dom (dom path)
     :as-data (xml/parse path)}))
