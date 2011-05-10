(ns mycroft.data
  (:use clojure.pprint
        [clojure.tools.logging :only (info)]
        mycroft.selector
        [hiccup.core :only (escape-html)])
  (:require [mycroft.docs :as docs]
            [mycroft.breadcrumb :as breadcrumb]))

(defn indexed
  "Returns a lazy sequence of [index, item] pairs, where items come
  from 's' and indexes count up from zero.

  (indexed '(a b c d))  =>  ([0 a] [1 b] [2 c] [3 d])"
  [s]
  (map vector (iterate inc 0) s))

(declare render-collection render-string)

(defmulti keyed class)
(defmethod keyed java.util.Set [obj] (indexed obj))
(defmethod keyed clojure.lang.Sequential [obj] (indexed obj))
(defmethod keyed :default [obj] obj)

(defn safe-deref
  [v]
  (try
   (deref v)
   (catch IllegalStateException e e)))

(defn tag
  "Like class, but partitions all arrays under the keyword :Array."
  [t]
  (cond
   (nil? t) nil
   (-> t .getClass .isArray) :Array
   :else (class t)))

(defmulti render-type (fn [type options] (tag type)))
(defmethod render-type nil [this options]
  (render-type "<nil>" options))
(defmethod render-type :Array [this options]
  (render-type (seq this) options))
(defmethod render-type java.util.Collection [this options]
  (render-collection this options))
(defmethod render-type clojure.lang.ISeq [this options]
  (render-collection this options))
(defmethod render-type clojure.lang.IPersistentCollection [this options]
  (render-collection this options))
(defmethod render-type clojure.lang.IDeref [this options]
  (render-type (deref this 0 :pending) (add-selector options :mycroft/deref)))
(defmethod render-type clojure.lang.Var [this options]
  (if (fn? (safe-deref this))
    (docs/render this options)
    (render-type (safe-deref this) (add-selector options :mycroft/deref))))
(defmethod render-type :default [this options]
  (render-string this options))

(prefer-method render-type clojure.lang.IPersistentCollection java.util.Collection)

(defn render
  "Given a var and some options, render the var
   as html. Options:

   :selectors : vector is passed to select to drill in
   :start    : start at the nth item
   :count    : how many items to show"
  [var options]
  (let [selectors (:selectors options)
        selection (select-in var selectors)]
    [:div
     (breadcrumb/render var options selection)
     (render-type selection options)]))

(defn render-string
  [content options]
  [:pre (escape-html (str content))])

(defn abbreviate
  "Render the item with print settings so only part of the
   item is shown."
  [item]
  (binding [*print-length* 5
            *print-level* 2]
    (with-out-str (pr item))))

(defn render-cell
  ([content] (render-cell content nil))
  ([content {:keys [href]}]
     (let [content-html (escape-html (abbreviate content))]
       [:td
        (if href
          [:a {:href href} content-html]
          content-html)])))

(defn render-row
  [row options]
  {:pre (= 2 (count row))}
  `[:tr
    ~(render-cell (first row) {:href (str "?" (breadcrumb/params->query-string (add-selector options (first row))))})
    ~@(map render-cell (rest row))])

(defn render-row-matching-headers
  [[key obj :as row] {:keys [headers] :as options}]
  {:pre [(= 2 (count row))
         (associative? obj)]}
  `[:tr
    ~(render-cell key {:href (str "?" (breadcrumb/params->query-string (add-selector options key)))})
    ~@(let [explicit-columns (map #(% obj) headers)]
        (map render-cell explicit-columns))
    ~(let [rest-of-object (apply dissoc obj headers)]
        (render-cell rest-of-object))])

(defn composite?
  [x]
  (or (seq? x)
      (-> x .getClass .isArray)
      (instance? clojure.lang.Seqable x)
      (instance? Iterable x)
      (instance? java.util.Map x)))

(def items-per-page 15)

(defn render-pagination
  [{:keys [start] :as params} count has-more?]
  (info "Pagination Count:" count)
  (info "Pagination Options:" params)
  (info "Pagination Has More?:" has-more?)
  (info "Pagination Start:" start)
  (when (or (not count)
            (> count items-per-page))
    [:div.buttons {:id "pagination"}
     (if (> start 0)
       [:a {:href (str "?" (breadcrumb/params->query-string (update-in params [:start] - items-per-page)))}
        "prev"]
       [:span.disabled-button "prev"])
     (when count
       [:span
        (str "Items " start "-" (min count (+ start items-per-page)) " of " count)])
     (if has-more?
       [:a {:href (str "?" (breadcrumb/params->query-string (update-in params [:start] + items-per-page)))}
        "next"]
       [:span.disabled-button "next"])]))

(defn render-table-with-headers
  [content {:keys [headers] :as options}]
  `[:table.data
    [:thead
     [:tr
      ~@(map #(vector :td %) (concat ["&nbsp;"] headers))]]
    [:tbody
     ~@(map
        (fn [o]
          (render-row-matching-headers o options))
        content)]])

(defn render-table
  [content {:keys [headers] :as options}]
  (if (seq content)
    (if headers
      (render-table-with-headers content options)
      [:table.data
       (map
        #(render-row % options)
        content)])
    [:table.data
     [:th "Collection is empty."]]))

(defn render-collection
  [content {:keys [selector start] :as options}]
  (let [count (when (counted? content) (count content))
        content (keyed content)
        content (if start (drop start content) content)
        has-more? (boolean (seq (drop items-per-page content)))
        content (take items-per-page content)]
    (if (composite? content)
      [:div
       (render-table content options)
       (render-pagination options count has-more?)]
      (render-type content options))))


