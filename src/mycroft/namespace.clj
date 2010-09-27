(ns mycroft.namespace
  (:require [mycroft.breadcrumb :as breadcrumb]))

(defn namespaces
  "Sorted list of namespaces"
  []
  (->> (all-ns)
       (sort-by #(.name %))))

(defn vars
  "Sorted list of var names in a namespace (symbols)."
  [ns]
  (when-let [ns (find-ns (symbol ns))]
    (sort-by #(str (.sym %)) (vals (ns-publics ns)))))

(defn browser
  ([] (browser (namespaces)))
  ([nses]
     [:div
      [:div
       (breadcrumb/render nil nil nil)]
      [:ul
       (map
        (fn [ns] [:li (breadcrumb/link-to ns)])
        nses)]]))

(defn safe-load-ns
  [ns]
  (try
   (require (symbol ns))
   (catch java.io.IOException _)))

(defn var-browser
  [ns]
  [:div
   (breadcrumb/render ns nil nil)
   [:ul
    (map
     (fn [var] [:li (breadcrumb/link-to var)])
     (vars ns))]])



