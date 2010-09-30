(ns mycroft.diff
  (:require [clojure.set :as set])
  (:use [clojure.pprint :only (pprint)]))

(defn- atom-diff
  "Internal helper for diff."
  [a b]
  (if (= a b)
    [nil nil a]
    [a b nil]))

(declare diff)

(defprotocol Diff
  (diff-partition [x])
  (diff-similar [a b]))

(extend Object
        Diff
        {:diff-similar atom-diff
         :diff-partition (fn [x] (if (.. x getClass isArray) :sequential :atom))})

(extend-protocol Diff
  java.util.Set
  (diff-partition [x] :set)
  (diff-similar [a b]
                [(not-empty (set/difference a b))
                 (not-empty (set/difference b a))
                 (not-empty (set/intersection a b))])
  
  java.util.Collection
  (diff-partition [x] :sequential)
  (diff-similar [a b]
        (let [a-cnt (count a)
              b-cnt (count b)
              shared (min a-cnt b-cnt)
              biggest (if (< a-cnt b-cnt) b a)]
          (let [subdiffs (map
                          (fn [k] (map #(when % [k %]) (diff (nth a k) (nth b k))))
                          (range shared))] ;; slow, fix later
            (pprint {:subdiffs subdiffs})
            (reduce
             (fn [diff1 diff2]
                (map (fn [d1 [k v]] (if k (assoc d1 k v) d1)) diff1 diff2))
             [(into (vec (repeat shared nil)) (when (> a-cnt b-cnt) (subvec a b-cnt)))
              (into (vec (repeat shared nil)) (when (> b-cnt a-cnt) (subvec b a-cnt)))
              (vec (repeat shared nil))] 
             subdiffs))))
  
  java.util.Map
  (diff-partition [x] :map)
  (diff-similar [a b]
        (let [xkeys (set (keys a))
              ykeys (set (keys b))
              [only-a only-b shared] (diff xkeys ykeys)]
          #_(pprint {:xkeys xkeys
                   :ykeys ykeys
                   :only-a only-a
                   :only-b only-b})
          (let [subdiffs (map
                          (fn [k] (map #(when % {k %}) (diff (get a k) (get b k))))
                          shared)]
            #_(pprint {:subdiffs subdiffs})
            (reduce
             (fn [diff1 diff2]
               (map merge diff1 diff2))
             [(not-empty (select-keys a only-a))
              (not-empty (select-keys b only-b))
              nil]
             subdiffs)))))

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
  
