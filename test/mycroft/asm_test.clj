(ns mycroft.asm-test
  (:use mycroft.asm clojure.test clojure.pprint))

(defn compare-reflections
  [r1 r2]
  (is (= (:bases r1) (:bases r2)))
  (is (= (:fields r1) (:fields r2)))
  (is (= (:constructors r1) (:constructors r2)))
  (is (= (:methods r1) (:methods r2))))

(deftest compare-reflect-and-asm
  (doseq [classname '[java.lang.Runnable
                      java.lang.Object]]
    (compare-reflections (asm-reflect classname) (java-reflect classname))))
