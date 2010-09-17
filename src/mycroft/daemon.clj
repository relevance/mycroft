(ns mycroft.daemon
  (:use [clojure.contrib.java-utils :only (file)]
        [mycroft.main  :only (-main)]))

(defn daemonize
  []
  (.deleteOnExit (file "log/daemon.pid"))
  (.. System out close)
  (.. System err close)
  (-main))