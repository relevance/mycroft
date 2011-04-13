(ns mycroft.history
  (:require [mycroft.breadcrumb :as breadcrumb]))

(def history (atom []))

(defn add
  "Add an object to history, returning its URL."
  [obj]
  (str "/vars/mycroft.history/history?"
       (breadcrumb/params->query-string {:selectors [:mycroft/deref :mycroft/deref (dec (count (swap! history conj obj)))]})))




