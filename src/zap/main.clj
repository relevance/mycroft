(ns zap.main
  (:use [clojure.contrib.json :only [json-str]]
        [ring.adapter.jetty :only (run-jetty)]
        [ring.util.response :only (redirect)]
        [compojure.core :only (defroutes GET POST)]
        [hiccup.core :only (html)]
        [zap.html :only [layout minib-layout]]
        clojure.pprint)
  (:require
   clojure.repl
   [compojure.route :as route]
   [zap.jmx :as jmx]
   [clojure.string :as str]))

(defn indexed
  "Returns a lazy sequence of [index, item] pairs, where items come
  from 's' and indexes count up from zero.

  (indexed '(a b c d))  =>  ([0 a] [1 b] [2 c] [3 d])"
  [s]
  (map vector (iterate inc 0) s))

(defn keyed
  [expr]
  (if (map? expr)
    expr
    (indexed expr)))

(defprotocol IdComponent
  (as-id [this]))

(extend-protocol IdComponent
  clojure.lang.Keyword
  (as-id [this] (as-id (name this)))

  java.lang.Object
  (as-id [this] (-> (str this) (str/replace #"[^A-Za-z0-9]" "-"))))

(defn make-id
  [path]
  (str/join "--" (map as-id path)))

(defn gui-item
  [path item]
  (cond
   (associative? item)
   [:a {:href (str "#" (make-id path))} (last path)]
   :else item))

(defn title
  [path]
  (str/join " " (drop (- (count path) 1) path)))

(defn list-item
  [path i elem]
  (let [item (gui-item (conj path i) elem)]
    (if (or (number? i)
            (associative? elem))
      [:li item]
      [:li [:span i] "&nbsp;&nbsp;&nbsp;"[:span item]])))

(defn gui-list
  [path expr]
  [:ul
   (map
    (fn [[i e]] (list-item path i e))
    (keyed expr))])

                                        ; html tables seem to screw up jqtouch
(defn gui-table
  [path expr]
  [:ul
   (map
    (fn [[i e]] (list-item path i e))
    (keyed expr))]
  #_[:table
     (map
      (fn [[i e]] [:tr [:td i] [:td (gui-item (conj path i) e)]])
      (keyed expr))])

(defn gui-seq
  ([expr]
     (gui-seq ["top"] expr))
  ([path expr]
     (cond
      (associative? expr)
      (cons
       [:div {:id (make-id path)}
        [:div {:class "toolbar"} [:h1 (title path)]]
        (gui-table path expr)]
       (->> (keyed expr)
            (filter (fn [[_ e]] (associative? e)))
            (mapcat (fn [[i e]] (gui-seq (conj path i) e)))))
      :else expr)))

(defn namespace-names
  "Sorted list of namespace names (strings)."
  []
  (->> (all-ns)
       (map #(.name %))
       (sort)))

(defn var-names
  "Sorted list of var names in a namespace (symbols)."
  [ns]
  (when-let [ns (find-ns (symbol ns))]
    (sort (keys (ns-publics ns)))))

(defn namespace-link
  [ns-name]
  [:a {:href (str "/browse/" ns-name)} ns-name])

(defn namespace-browser
  [ns-names]
  [:div
   {:class "browse-list"}
   [:ul
    (map
     (fn [ns] [:li (namespace-link ns)])
     ns-names)]])

(defn var-link
  [ns-name var-name]
  [:a {:href (str "/browse/" ns-name "/" (java.net.URLEncoder/encode (str var-name)))} var-name])

(defn var-browser
  [ns vars]
  (html
   [:div
    {:class "browse-list variables"}
    [:ul
     (map
      (fn [var] [:li (var-link ns var)])
      vars)]]))

(defn view-function
  [func]
  (html
   [:h3 (find-var (symbol func))]))

(defn var-symbol
  "Create a var-symbol, given the ns and var names as strings."
  [ns var]
  (symbol (str ns "/" var)))

(defn format-code
  [& codes]
  (apply str (map
              (fn [code]
                (if (string? code)
                  (str code "\n")
                  (with-out-str (pprint code))))
              codes)))

(defn one-liner?
  [s]
  (if s
    (< (count (remove empty? (str/split s #"\s*\n\s*"))) 2)
    true))

(defn code*
  "Show codes (literal strings or forms) in a pre/code block."
  [& codes]
  (let [code-string (apply format-code codes)
        class-string "brush: clojure; toolbar: false;"
        class-string (if (one-liner? code-string) (str class-string  " light: true;") class-string)]
    [:script {:type "syntaxhighlighter" :class class-string}
     (str "<![CDATA[" code-string "]]>")]))

(defn var-detail
  [ns var]
  (when var
    (let [sym (var-symbol ns var)
          var (find-var sym)]
      (html [:h3 sym]
            [:h4 "Docstring"]
            [:pre [:code
                   (with-out-str (print-doc var))]]
            [:h4 "Source"]
            (code* (clojure.repl/source-fn sym))))))

(defroutes browser-routes
  (GET "/" [] (layout))
  (GET "/stuff" [] (html (gui-seq (jmx/beans "*:*"))))
  (GET "/browse" []
       (html
        (minib-layout
         (namespace-browser (namespace-names))
         [:div {:class "browse-list empty"}])))
  (GET
   "/browse/*"
   request
   (let [[ns var] (str/split (get-in request [:params "*"]) #"/")]
     (html
      (minib-layout
       (if var
         (var-detail ns var)
         (var-browser ns (var-names ns)))))))
  (route/files "/")
  (route/not-found "not found"))

(defn -main []
  (run-jetty (var browser-routes) {:port 8080
                                   :join? false}))
