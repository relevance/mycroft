(ns mycroft.main
  (:require mycroft.server))

(def inspector (mycroft.server.Instance. 8080))

(defn inspect
  [o]
  (.inspect inspector o))

(.launch inspector)

