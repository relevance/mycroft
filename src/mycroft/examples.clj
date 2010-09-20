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

(def heterogeneous
  [42
   3.14159
   "foo"
   :keyword
   {:fname "John" :lname "Doe"}])

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

(def browse-history (atom (list)))

(defn append-to-history
  [request]
  (swap! browse-history #(->> % (cons request) (take 50))))

(def metrics
  (let [ns (filter #(-> % (.getName) str (.startsWith "mycroft"))
                   (all-ns))
        var-count (apply + (map #(count (ns-interns %)) ns))]
    {:metrics-for "Mycroft"
     :namespaces (count ns)
     :vars var-count}))

(defn with-recent-history [handler]
  (fn [request]
    (let [response (handler request)]
      (when response
        (append-to-history request)
        response))))

