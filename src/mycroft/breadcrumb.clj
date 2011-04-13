(ns mycroft.breadcrumb
  (:use mycroft.selector
        [hiccup.page-helpers :only (encode-params)]
        [hiccup.core :only (escape-html)])
  (:require [mycroft.docs :as docs]))

(defn params->query-string
  "Generate query string for the params provided. Params include

   :selectors                  vector of selectors for select-in
   :start                      first item to show (for pagination)
   :headers                    explicitly select table headers."
  [params]
  (encode-params (select-keys params [:selectors :start :headers])))

(defprotocol Resource
  (url-for [o])
  (link-name [o]))

(extend-protocol Resource
  Object
  (url-for [o] nil)
  (link-name [o] (str o))
  
  clojure.lang.Var
  (url-for
   [v]
   (let [ns-name (.. v ns name)
         var-name (.. v sym getName)]
     (str "/vars/" ns-name "/" (java.net.URLEncoder/encode (str var-name)))))
  (link-name
   [v] (.sym v))

  clojure.lang.Namespace
  (url-for
   [ns]
   (str "/vars/" (.name ns)))
  (link-name
   [ns]
   (.name ns))
  
  Class
  (url-for
   [o]
   (let [classname (.getName o)]
     (str "/classes/" classname)))
  (link-name
   [c]
   (.getName c)))

(defn link-to
  [o]
  (if-let [url (url-for o)]
    [:a {:href url} (link-name o)]
    (escape-html (link-name o))))

(defn top-link
  []
  [:a {:href (str "/vars")} "top"])

(defn breadcrumb-text
  "Convert the internal names for meta and deref into user-friendly terms,
   everything else renders unchanged."
  [selector-component]
  (get {:mycroft/deref "@" :mycroft/meta "&lt;meta&gt;"} selector-component selector-component))

(defn render
  [item options selection]
  (let [ns (when (var? item) (.ns item))
        var (when (var? item) item)
        classname (when (class? item) (.getName item))
        selectors (:selectors options)]
    [:div
     [:div {:id "breadcrumb"}
      (if ns (top-link) "top")
      (when ns
        [:span " &laquo; "
         (if var (link-to ns) ns)])
      (when classname
        [:span " &laquo; "
         (if selectors (link-to item) (link-name item))])
      (when var
        [:span "&nbsp;/&nbsp;"
         (if selectors (link-to var) (link-name var))])

      (when selectors
        (let [first-crumb (if (= :mycroft/deref (first selectors)) 2 1)]
          [:span
           (->> (map (fn [n] (subvec selectors 0 n)) (range first-crumb (count selectors)))
                (map (fn [partial-selectors]
                       [:span
                        " &raquo; "
                        [:a {:href (str "?" (params->query-string {:selectors partial-selectors}))}
                         (breadcrumb-text (last partial-selectors)) ]])))
           [:span " &raquo; " (breadcrumb-text (last selectors))]]))]
     [:div#options
      (when (meta selection)
        [:span
         [:a {:href (str "?" (params->query-string (add-selector options :mycroft/meta)))} "metadata"]])
      (when-let [class (and selection (.getClass selection))]
        [:span (link-to class)])
      (when-let [doc-url (docs/doc-url selection)]
        [:span
         [:a {:href doc-url} "api docs"]])]]))

