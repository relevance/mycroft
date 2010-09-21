(ns mycroft.daemon
  (:use [clojure.contrib.java-utils :only (file)])
  (:require mycroft.main))

(defn daemonize
  []
  (.deleteOnExit (file "log/daemon.pid"))
  (.. System out close)
  (.. System err close)
  (mycroft.main/run 8080))
