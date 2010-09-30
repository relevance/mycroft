(ns mycroft.asm-test
  (:use mycroft.asm clojure.test clojure.pprint))

(defn compare-reflections
  [r1 r2]
  (is (= (:bases r1) (:bases r2)))
  (is (= (:fields r1) (:fields r2)))
  (is (= (:constructors r1) (:constructors r2)))
  (is (= (:methods r1) (:methods r2))))

(defn nodiff
  [x y]
  (let [[x-only y-only common] (diff x y)]
    (when (or x-only y-only)
      (is false (with-out-str (pprint {:x-only x-only
                                       :y-only y-only
                                       :common common}))))))

(deftest compare-reflect-and-asm
  (doseq [classname '[java.lang.Runnable
                      java.lang.Object
                      #_java.io.FileInputStream]]
    (nodiff (asm-reflect classname) (java-reflect classname))))

(deftest diff-test
  (are [d x y] (= d (diff x y))
       [1 2 nil] 1 2
       [#{:a} #{:b} #{:c :d}] #{:a :c :d} #{:b :c :d}
       [nil nil {:a 1}] {:a 1} {:a 1}
       [{:a #{2}} {:a #{4}} {:a #{3}}] {:a #{2 3}} {:a #{3 4}}))

