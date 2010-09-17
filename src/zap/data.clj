(ns zap.data
  (:use [hiccup.page-helpers :only (encode-params)])
  (:require [zap.docs :as docs]))

(defn indexed
  "Returns a lazy sequence of [index, item] pairs, where items come
  from 's' and indexes count up from zero.

  (indexed '(a b c d))  =>  ([0 a] [1 b] [2 c] [3 d])"
  [s]
  (map vector (iterate inc 0) s))

(declare render-table render-string)

(defmulti keyed class)
(defmethod keyed java.util.Set [obj] (map vector obj))
(defmethod keyed clojure.lang.Sequential [obj] (indexed obj))
(defmethod keyed :default [obj] obj)

(defmulti render-type (fn [type options] (class type)))
(defmethod render-type nil [this options]
  (render-type "<nil> !!" options))
(defmethod render-type java.util.Collection [this options]
  (render-table this options))
(defmethod render-type clojure.lang.IPersistentCollection [this options]
  (render-table this options))
(defmethod render-type clojure.lang.Fn [this options]
  (docs/render this options))
(defmethod render-type :default [this options]
  (render-string this options))

(prefer-method render-type clojure.lang.IPersistentCollection java.util.Collection)

(use 'clojure.pprint)
(defn select
  "Like get-in, but uses nth to follow (in O(n) time!)
   lazy sequences."
  [item selectors]
  (reduce
   (fn [item sel]
     (pprint {:item item
              :sel sel})
     (if (integer? sel)
       (nth item sel)
       (get item sel)))
   item
   selectors))

(defn paginate
  "Given a var and some options, find the part of a var
   to show on this page. Options:

   :selector : vector is passed to select to drill in
   :meta     : true to look at metadata instead of data
   :start    : start at the nth item
   :count    : how many items to show"
  [item {:keys [selector start count meta] :as options}]
  (let [item (if (var? item)
               (if meta (meta item) @item)
               item)
        item (if (instance? clojure.lang.IRef item) @item item)
        item (if selector (select item selector) item)
        item (keyed item)
        item (if start (drop start item) item)
        item (if count (take count item) item)]
    item))

(defn normalize-options
  "Convert options from string form (as coming in from web)
   to data structures as needed."
  [options]
  (if (:selector options)
    (update-in options [:selector] read-string)
    options))

(defn render
  [obj options]
  (let [options (normalize-options options)]
    (render-type (paginate obj options) options)))

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

(defn add-selector [options s]
  (if (:selector options)
    (update-in options [:selector] conj s)
    (assoc options :selector [s])))

(defn url
  [options]
  (str "?" (encode-params options)))

(defn render-row
  [row options]
  (if (second row)
    `[:tr
      ~(render-cell (first row) {:href (url (add-selector options (first row)))})
      ~@(map render-cell (rest row))]
    [:tr (render-cell (first row))]))

(defn render-table
  [content options]
  [:table.data
   (map
    #(render-row % options)
    content)])

