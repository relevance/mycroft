(ns mycroft.jqtouch
  (:use [hiccup.core :only (html)]
        [hiccup.page-helpers :only (doctype include-js include-css)])
  (:require [clojure.string :as str]))

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

(defn layout []
  (html
   (doctype :html5)
   [:head
    [:title "jQTouch &beta;"]
    (include-js "/jqtouch/jquery.1.3.2.min.js"
                "/jqtouch/jqtouch.min.js"
                "/javascripts/minib.js")
    (include-css "/jqtouch/jqtouch.min.css"
                 "/jqtouch/themes/jqt/theme.min.css")]
   [:body
    [:div {:id "placeholder" :class "current"}
     [:ul
      [:li "loading..."]]]
    [:div {:id "content"}]]))

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

