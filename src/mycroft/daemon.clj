(ns mycroft.daemon
  (:use [clojure.contrib.java-utils :only (file)]))

(defn daemonize
  []
  (.deleteOnExit (file "log/daemon.pid"))
  (.. System out close)
  (.. System err close)
  ;; mycroft.main launches the inspector when loaded
  (require 'mycroft.main))
