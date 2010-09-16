(ns zap.data
  (:use [hiccup.page-helpers :only (encode-params)]))

(defn indexed
  "Returns a lazy sequence of [index, item] pairs, where items come
  from 's' and indexes count up from zero.

  (indexed '(a b c d))  =>  ([0 a] [1 b] [2 c] [3 d])"
  [s]
  (map vector (iterate inc 0) s))

(declare render-table render-string)

(defprotocol Helper
  (keyed [this])
  (render-type [this options]))

(extend-protocol Helper
  nil
  (keyed [_] nil)
  (render-type [this options] (render-type "<nil>" options))
  
  clojure.lang.IRef
  (keyed [this] (keyed @this))
  (render-type [this options] (render-type @this options))
  
  clojure.lang.Sequential
  (keyed [this] (indexed this))
  (render-type [this options] (render-table this options))
  
  java.util.Map
  (keyed [this] this)
  (render-type [this options] (render-table this options))
  
  java.util.Set
  (keyed [this] (map vector this))
  (render-type [this options] (render-table this options))

  java.lang.Object
  (keyed [this] this)
  (render-type [this options] (render-string this options)))

(use 'clojure.pprint)
(defn paginate
  [item {:keys [selector start count meta] :as options}]
  (pprint {:item item})
  (let [item (if (var? item)
               (if meta (meta item) @item)
               item)
        item (if selector (get-in item selector) item)
        item (keyed item)
        item (if start (drop start item) item)
        item (if count (take count item) item)]
    item))

(defn normalize-options
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

