(ns mycroft.breadcrumb
  (:use [hiccup.page-helpers :only (encode-params)]))

(defn options->query-string
  "Generate query string for the options provided. Options include

   :selectors                  vector of selectors for select-in
   :start                      first item to show (for pagination)
   :headers                    explicitly select table headers."
  [options]
  (encode-params options))

(defn namespace-link
  [ns-name]
  [:a {:href (str "/vars/" ns-name)} ns-name])

(defn top-link
  []
  [:a {:href (str "/vars")} "top"])

(defn var-link
  [ns-name var-name]
  [:a {:href (str "/vars/" ns-name "/" (java.net.URLEncoder/encode (str var-name)))} var-name])

(defn breadcrumb-text
  "Convert the internal names for meta and deref into user-friendly terms,
   everything else renders unchanged."
  [selector-component]
  (get {:mycroft.data/deref "@" :mycroft.data/meta "&lt;meta&gt;"} selector-component selector-component))

(defn render
  [ns var {:keys [selectors]}]
  [:div {:id "breadcrumb"}
   (if ns (top-link) "ns")
   (when ns
     [:span " &laquo; "
      (if var (namespace-link ns) ns)])
   (when var
     [:span "&nbsp;/&nbsp;"
      (if selectors (var-link (.ns var) (.sym var)) (.sym var))])

   (when selectors
     (let [first-crumb (if (= ::deref (first selectors)) 2 1)]
       [:span
        (->> (map (fn [n] (subvec selectors 0 n)) (range first-crumb (count selectors)))
             (map (fn [partial-selectors]
                    [:span
                     " &raquo; "
                     [:a {:href (str "?" (options->query-string {:selectors partial-selectors}))}
                      (breadcrumb-text (last partial-selectors)) ]])))
        [:span " &raquo; " (breadcrumb-text (last selectors))]]))])

