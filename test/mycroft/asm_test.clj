(ns mycroft.asm-test
  (:use mycroft.asm clojure.test clojure.pprint))

(deftest compare-reflect-and-asm
  (println "== asm ==")
  (pprint (asm-reflect 'java.io.Serializable))
  (println "== reflect ==")
  (pprint (java-reflect 'java.io.Serializable)))
