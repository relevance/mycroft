(ns zap.data)

(defn indexed
  "Returns a lazy sequence of [index, item] pairs, where items come
  from 's' and indexes count up from zero.

  (indexed '(a b c d))  =>  ([0 a] [1 b] [2 c] [3 d])"
  [s]
  (map vector (iterate inc 0) s))

(declare render-table render-string)

(defprotocol Helper
  (keyed [this])
  (render [this options]))

(extend-protocol Helper
  clojure.lang.IRef
  (keyed [this] (keyed @this))
  (render [this options] (render @this options))
  
  clojure.lang.Sequential
  (keyed [this] (indexed this))
  (render [this options] (render-table this options))
  
  java.util.Map
  (keyed [this] this)
  (render [this options] (render-table this options))
  
  java.util.Set
  (keyed [this] (map vector this))
  (render [this options] (render-table this options))

  java.lang.Object
  (keyed [this] this)
  (render [this options] (render-string this options)))

(defn paginate
  [item {:keys [selector start count]}]
  (let [item (if selector (get-in item selector) item)
        item (keyed item)
        item (if start (drop start item) item)
        item (if count (take count item) item)]
    item))

(defn render-string
  [content options]
  [:pre (str content)])

(defn render-cell
  [content]
  (str content))

(defn render-table
  [content options]
  (let [item (paginate content options)]
    [:table
     (map
      (fn [row] [:tr (map (fn [col] [:td (render-cell col)]) row)])
      item)]))

