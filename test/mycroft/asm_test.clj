(ns mycroft.asm-test
  (:use mycroft.asm clojure.test clojure.pprint))

(deftest compare-reflect-and-asm
  (pprint (asm-reflect 'java.io.Serializable))
  (pprint (java-reflect 'java.io.Serializable)))
