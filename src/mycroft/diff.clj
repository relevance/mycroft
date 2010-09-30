(ns mycroft.diff
  (:require [clojure.set :as set])
  (:use [clojure.pprint :only (pprint)]))

(defn- atom-diff
  "Internal helper for diff."
  [a b]
  (if (= a b) [nil nil a] [a b nil]))

;; for big things a sparse vector class would be better
(defn- vectorize
  "Convert an associative-by-numeric-index collection into
   an equivalent vector, with nil for any missing keys"
  [m]
  (when (seq m)
    (reduce
     (fn [result [k v]] (assoc result k v))
     (vec (repeat (apply max (keys m))  nil))
     m)))

(declare diff)

(defprotocol Diff
  (diff-partition [x])
  (diff-similar [a b]))

(extend Object
        Diff
        {:diff-similar atom-diff
         :diff-partition (fn [x] (if (.. x getClass isArray) :sequential :atom))})

(defn diff-associative
  "Diff associative things a and b, comparing only keys in ks."
  [a b ks]
  (reduce
   (fn [diff1 diff2]
     (map merge diff1 diff2))
   [nil nil nil]
   (map
    (fn [k] (map #(when % {k %}) (diff (get a k) (get b k))))
    ks)))

(extend-protocol Diff
  nil
  (diff-partition [_] :nil)
  (diff-similar [_ _] [nil nil nil])
  
  java.util.Set
  (diff-partition [x] :set)
  (diff-similar [a b]
    [(not-empty (set/difference a b))
     (not-empty (set/difference b a))
     (not-empty (set/intersection a b))])
  
  java.util.Collection
  (diff-partition [x] :sequential)
  (diff-similar [a b]
    (vec (map vectorize (diff-associative
                         (if (vector? a) a (vec a))
                         (if (vector? b) b (vec b))
                         (range (max (count a) (count b)))))))
  
  java.util.Map
  (diff-partition [x] :map)
  (diff-similar [a b]
    (diff-associative a b (set/union (keys a) (keys b)))))

(defn diff
  "Recursively compares a and b, returning a tuple of
  [things-only-in-a things-only-in-b things-in-both].
  Comparison rules:

  * Maps are subdiffed where keys match and values differ.
  * Sets are never subdiffed.
  * All sequential things are treated as associative collections
    by their indexes, with results returned as vectors.
  * Everything else (including strings!) is treated as
    an atom and compared for equality."
  [a b]
  (if (= (diff-partition a) (diff-partition b))
    (diff-similar a b)
    (atom-diff a b)))
  
