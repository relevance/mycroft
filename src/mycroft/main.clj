(ns mycroft.main
  (:require
   mycroft.server
   [clojure.edn :as edn]))

(def ^{:doc "Instance of the currently running inspector."}
  inspector nil)

(defn -main
  "Run the inspector web server on the specified port. There
   is currently no facility for in-process shutdown and
   restart (though this could easily be added)."
  [port]
  (let [port (when (string? port) (edn/read-string port))]
    (alter-var-root #'inspector (constantly (mycroft.server.Instance. port)))
    (.launch inspector)
    (refer 'mycroft.main :only '(inspect))
    (clojure.main/repl)))

(defmacro inspect
  "Primary entry point for Clojure clients. You should be able to
   inspect anything, including vars, classes, and arbitrary
   expressions. A history of past things you have inspected is
   at /vars/mycroft.history/history."
  [o & options]
  (let [options (apply hash-map options)]
    (if inspector
      (if (symbol? o)
        (if-let [resolved (ns-resolve *ns* o)]
          `(.inspect inspector ~resolved ~options)
          `(.inspect inspector '~o ~options))
        `(.inspect inspector ~o ~options))
      "Launch the inspector with (mycroft.main/run port) first!")))



