(ns mycroft.main
  (:require mycroft.server))

(def ^{:doc "Instance of the currently running inspector."}
  inspector nil)

(defn run
  "Run the inspector web server on the specified port. There
   is currently no facility for in-process shutdown and
   restart (though this could easily be added)."
  [port]
  (alter-var-root #'inspector (constantly (mycroft.server.Instance. port)))
  (.launch inspector))

(defmacro inspect
  "Primary entry point for Clojure clients. You should be able to
   inspect anything, including vars, classes, and arbitrary
   expressions. A history of past things you have inspected is
   at /vars/mycroft.history/history."
  [o]
  (if inspector
    (if (symbol? o)
      (if-let [resolved (ns-resolve *ns* o)]
        `(.inspect inspector ~resolved)
        `(.inspect inspector '~o))
      `(.inspect inspector ~o))
    
    "Launch the inspector with (mycroft.main/run port) first!"))



