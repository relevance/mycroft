(ns mycroft.data
  (:use clojure.pprint
        [hiccup.core :only (escape-html)]
        [clojure.contrib.core :only (.?.)])
  (:require [mycroft.docs :as docs]
            [mycroft.breadcrumb :as breadcrumb]))

(defn indexed
  "Returns a lazy sequence of [index, item] pairs, where items come
  from 's' and indexes count up from zero.

  (indexed '(a b c d))  =>  ([0 a] [1 b] [2 c] [3 d])"
  [s]
  (map vector (iterate inc 0) s))

(defn special-selector?
  [selector]
  (and (keyword? selector)
       (= (namespace selector) "mycroft.data")))

(defn add-selector
  [options s]
  (let [options (if (:selector options)
                  (update-in options [:selector] conj s)
                  (assoc options :selector [s]))
        options (if (special-selector? s)
                  options
                  (dissoc options :start))]
    options))

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
(defmethod render-type clojure.lang.IRef [this options]
  (render-type @this (add-selector options ::deref)))
(defmethod render-type clojure.lang.Var [this options]
  (if (fn? (safe-deref this))
    (docs/render this options)
    (render-type (safe-deref this) (add-selector options ::deref))))
(defmethod render-type :default [this options]
  (render-string this options))

(prefer-method render-type clojure.lang.IPersistentCollection java.util.Collection)

(defn select
  "Like get-in on steroids.

   * uses nth to follow (in O(n) time!) lazy sequences.
   * follows magic key mycroft.data/meta to metadata"
  [item selectors]
  (reduce
   (fn [item sel]
     (cond
      (= sel ::deref) @item
      (= sel ::meta) (meta item)
      (associative? item) (get item sel)
      (set? item) (nth (seq item) sel)
      (integer? sel) (nth item sel)))
   item
   selectors))

(defn render
  "Given a var and some options, render the var
   as html. Options:

   :selector : vector is passed to select to drill in
   :meta     : true to look at metadata instead of data
   :start    : start at the nth item
   :count    : how many items to show"
  [var options]
  (let [selector (:selector options)
        selection (select var selector)]
    [:div
     (breadcrumb/render (.ns var) var options)
     [:div.buttons
      (if (meta selection)
        [:span
         [:a {:href (breadcrumb/url (add-selector options ::meta))} "metadata"]]
        [:span.disabled-button "metadata"])
      (if-let [classname (.?. selection getClass getName)]
        [:span
         [:a {:href (str "/classes/" classname)} (str "class " classname)]]
        [:span.disabled-button "no class"])
      (if-let [doc-url (docs/doc-url selection)]
        [:span
         [:a {:href doc-url} "api docs"]]
        [:span.disabled-button "api docs"])]
     (render-type selection options)]))

(defn render-string
  [content options]
  [:pre (escape-html (str content))])

(defn abbreviate
  [item]
  (binding [*print-length* 5
            *print-level* 2]
    (str item)))

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
  (if (second row)
    `[:tr
      ~(render-cell (first row) {:href (breadcrumb/url (add-selector options (first row)))})
      ~@(map render-cell (rest row))]
    [:tr (render-cell (first row))]))

(defn composite?
  [x]
  (or (seq? x)
      (-> x .getClass .isArray)
      (instance? clojure.lang.Seqable x)
      (instance? Iterable x)
      (instance? java.util.Map x)))

(def items-per-page 15)

(defn render-pagination
  [{:keys [start] :as options} count has-more?]
  (when (or (not count)
            (> count items-per-page))
    [:div.buttons {:id "pagination"}
     (if (> start 0)
       [:a {:href (breadcrumb/url (update-in options [:start] - items-per-page))}
        "prev"]
       [:span.disabled-button "prev"])
     (when count
       [:span
        (str "Items " start "-" (min count (+ start items-per-page)) " of " count)])
     (if has-more?
       [:a {:href (breadcrumb/url (update-in options [:start] + 0 items-per-page))}
        "next"]
       [:span.disabled-button "next"])]))

(defn render-table
  [content {:keys [start] :as options}]
  (if (seq content)
    [:table.data
     (map
      #(render-row % options)
      content)]
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


