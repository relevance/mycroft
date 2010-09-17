(ns mycroft.examples
  (:require [mycroft.jmx :as jmx]))

(let [abc [:A :B :C :D :E]]
  (def an-atom (atom abc))
  (def a-vector abc)
  (def a-list (apply list abc))
  (def a-set (set abc))
  (def a-map (zipmap abc (iterate inc 1)))
  (def an-array (into-array abc))
  (def a-ref (ref abc)))

(def set-of-maps
  #{{:A 1 :B 2}
    {:A 3 :C 4}})

(def some-strings
  ["these" "are" "fine"])

(def a-nested-thing
  {:the-atom an-atom
   :the-vector a-vector
   :the-list a-list
   :the-set a-set
   :the-map a-map
   :the-ref a-ref})

(def jmx-beans
  (jmx/beans "*:*"))
