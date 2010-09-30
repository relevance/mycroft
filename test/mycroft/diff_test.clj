(ns mycroft.diff-test
  (:use mycroft.diff clojure.test))

(deftest diff-test
  (are [d x y] (= d (diff x y))
       [nil nil nil] nil nil
       [1 2 nil] 1 2
       [nil nil [1 2 3]] [1 2 3] '(1 2 3)
       [1 [:a :b] nil] 1 [:a :b]
       [{:a 1} :b nil] {:a 1} :b
       [:team #{:p1 :p2} nil] :team #{:p1 :p2}
       [{0 :a} [:a] nil] {0 :a} [:a]
       [nil [nil 2] [1]] [1] [1 2]
       [nil nil [1 2]] [1 2] (into-array [1 2])
       [#{:a} #{:b} #{:c :d}] #{:a :c :d} #{:b :c :d}
       [nil nil {:a 1}] {:a 1} {:a 1}
       [{:a #{2}} {:a #{4}} {:a #{3}}] {:a #{2 3}} {:a #{3 4}}
       [{:a {:c [1]}} {:a {:c [0]}} {:a {:c [nil 2] :b 1}}] {:a {:b 1 :c [1 2]}} {:a {:b 1 :c [0 2]}}))

