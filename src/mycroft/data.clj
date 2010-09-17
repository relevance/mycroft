(ns mycroft.data
  (:use [hiccup.page-helpers :only (encode-params)]
        clojure.pprint)
  (:require [mycroft.docs :as docs]))

(defn indexed
  "Returns a lazy sequence of [index, item] pairs, where items come
  from 's' and indexes count up from zero.

  (indexed '(a b c d))  =>  ([0 a] [1 b] [2 c] [3 d])"
  [s]
  (map vector (iterate inc 0) s))

(defn add-selector [options s]
  (if (:selector options)
    (update-in options [:selector] conj s)
    (assoc options :selector [s])))

(declare render-collection render-string)

(defmulti keyed class)
(defmethod keyed java.util.Set [obj] (map vector obj))
(defmethod keyed clojure.lang.Sequential [obj] (indexed obj))
(defmethod keyed :default [obj] obj)

(defn tag
  [t]
  (cond
   (nil? t) nil
   (-> t .getClass .isArray) :Array
   :else (class t)))

(defmulti render-type (fn [type options] (tag type)))
(defmethod render-type nil [this options]
  (render-type "&lt;nil&gt;" options))
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
  (if (fn? @this)
    (docs/render this options)
    (render-type @this (add-selector options ::deref))))
(defmethod render-type :default [this options]
  (render-string this options))

(prefer-method render-type clojure.lang.IPersistentCollection java.util.Collection)

(defn- select
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
      (integer? sel) (nth item sel)))
   item
   selectors))

(defn url
  [options]
  (str "?" (encode-params options)))

(defn normalize-options
  "Convert options from string form (as coming in from web)
   to data structures as needed."
  [options]
  (if (:selector options)
    (update-in options [:selector] read-string)
    options))

(defn render
  "Given a var and some options, render the var
   as html. Options:

   :selector : vector is passed to select to drill in
   :meta     : true to look at metadata instead of data
   :start    : start at the nth item
   :count    : how many items to show"
  [obj options]
  (let [options (normalize-options options)
        selection (select obj (:selector options))]
    [:div
     [:a {:href (url (add-selector options ::meta))} "metadata"]
     (render-type selection options)]))

(defn render-string
  [content options]
  [:pre (str content)])

(defn render-cell
  ([content] (render-cell content nil))
  ([content {:keys [href]}]
     (let [content-html (str content)]
       [:td
        (if href
          [:a {:href href} content-html]
          content-html)])))

(defn render-row
  [row options]
  (if (second row)
    `[:tr
      ~(render-cell (first row) {:href (url (add-selector options (first row)))})
      ~@(map render-cell (rest row))]
    [:tr (render-cell (first row))]))

(defn composite?
  [x]
  (or (seq? x)
      (-> x .getClass .isArray)
      (instance? clojure.lang.Seqable x)
      (instance? Iterable x)
      (instance? java.util.Map x)))

(defn render-table
  [content options]
  [:table.data
   (map
    #(render-row % options)
    content)])

(defn render-collection
  [content {:keys [selector start count] :as options}]
  (let [content (keyed content)
        content (if start (drop start content) content)
        content (if count (take count content) content)]
    (if (composite? content)
      (render-table content options)
      (render-type content options))))


