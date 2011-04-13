(ns mycroft.daemon
  (:use [clojure.java.io :only (file)])
  (:require mycroft.main))

(defn daemonize
  "Helper function to launch the server, e.g. when running
   mycroft on http://inspector.clojure.org."
  []
  (.deleteOnExit (file "log/daemon.pid"))
  (.. System out close)
  (.. System err close)
  (mycroft.main/run 8080))
