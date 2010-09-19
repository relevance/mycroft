(ns mycroft.namespace
  (:require [mycroft.breadcrumb :as breadcrumb]))

(defn- namespace-names
  "Sorted list of namespace names (strings)."
  []
  (->> (all-ns)
       (map #(.name %))
       (sort)))

(defn- var-names
  "Sorted list of var names in a namespace (symbols)."
  [ns]
  (when-let [ns (find-ns (symbol ns))]
    (sort (keys (ns-publics ns)))))

(defn browser
  ([] (browser (namespace-names)))
  ([ns-names]
     [:div
      [:div
       (breadcrumb/render nil nil nil)]
      [:ul
       (map
        (fn [ns] [:li (breadcrumb/namespace-link ns)])
        ns-names)]]))

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
     (fn [var] [:li (breadcrumb/var-link ns var)])
     (var-names ns))]])



