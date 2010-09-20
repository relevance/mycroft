(ns mycroft.main)

(defn -main []
  (def inspector (mycroft.server.Instance. 8080))
  (.launch inspector))

(defn inspect
  [o]
  (.inspect inspector o))
