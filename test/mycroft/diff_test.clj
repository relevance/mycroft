(ns mycroft.diff-test
  (:use mycroft.diff clojure.test))

(deftest diff-test
  (are [d x y] (= d (diff x y))
       [1 2 nil] 1 2
       [nil nil [1 2]] [1 2] (into-array [1 2])
       [#{:a} #{:b} #{:c :d}] #{:a :c :d} #{:b :c :d}
       [nil nil {:a 1}] {:a 1} {:a 1}
       [{:a #{2}} {:a #{4}} {:a #{3}}] {:a #{2 3}} {:a #{3 4}}))

