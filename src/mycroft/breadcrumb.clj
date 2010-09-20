(ns mycroft.breadcrumb
  (:use [hiccup.page-helpers :only (encode-params)]))

(defn url
  [options]
  (str "?" (encode-params options)))

(defn namespace-link
  [ns-name]
  [:a {:href (str "/vars/" ns-name)} ns-name])

(defn top-link
  [ns]
  (if ns
    [:a {:href (str "/vars")} "top"]
    "top"))

(defn var-link
  [ns-name var-name]
  [:a {:href (str "/vars/" ns-name "/" (java.net.URLEncoder/encode (str var-name)))} var-name])

(defn breadcrumb-text
  "Convert the internal names for meta and deref into user-friendly terms,
   everything else renders unchanged."
  [selector-component]
  (get {:mycroft.data/deref "@" :mycroft.data/meta "&lt;meta&gt;"} selector-component selector-component))

(defn render
  [ns var {:keys [selector]}]
  [:div {:id "breadcrumb"}
   (top-link ns)
   (when ns
     [:span " &laquo; "
      (if var (namespace-link ns) ns)])
   (when var
     [:span "&nbsp;/&nbsp;"
      (if selector (var-link (.ns var) (.sym var)) (.sym var))])

   (when selector
     (let [first-crumb (if (= ::deref (first selector)) 2 1)]
       [:span
        (->> (map (fn [n] (subvec selector 0 n)) (range first-crumb (count selector)))
             (map (fn [partial-selector]
                    [:span
                     " &raquo; "
                     [:a {:href (url {:selector partial-selector})}
                      (breadcrumb-text (last partial-selector)) ]])))
        [:span " &raquo; " (breadcrumb-text (last selector))]]))])

