(ns mycroft.main
  (:require mycroft.server))

(def inspector nil)

(defn run
  [port]
  (alter-var-root #'inspector (constantly (mycroft.server.Instance. port)))
  (.launch inspector))

(defmacro inspect
  [o]
  (if inspector
    (if (symbol? o)
      (if-let [resolved (ns-resolve *ns* o)]
        `(.inspect inspector ~resolved)
        `(.inspect inspector '~o))
      `(.inspect inspector ~o))
    
    "Launch the inspector with (mycroft.main/run port) first!"))



