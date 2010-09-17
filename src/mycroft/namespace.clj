(ns mycroft.namespace)

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

(defn- namespace-link
  [ns-name]
  [:a {:href (str "/vars/" ns-name)} ns-name])

(defn browser
  ([] (browser (namespace-names)))
  ([ns-names]
     [:div
      {:class "browse-list"}
      [:ul
       (map
        (fn [ns] [:li (namespace-link ns)])
        ns-names)]]))

(defn- var-link
  [ns-name var-name]
  [:a {:href (str "/vars/" ns-name "/" (java.net.URLEncoder/encode (str var-name)))} var-name])

(defn safe-load-ns
  [ns]
  (try
   (require (symbol ns))
   (catch java.io.IOException _)))

(defn var-browser
  [ns]
  [:div
   {:class "browse-list variables"}
   [:ul
    (map
     (fn [var] [:li (var-link ns var)])
     (var-names ns))]])



