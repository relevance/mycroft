(ns zap.docs
  (:use [clojure.pprint :only (pprint)])
  (:require [clojure.string :as str]
            clojure.repl))

(defn format-code
  [& codes]
  (apply str (map
              (fn [code]
                (if (string? code)
                  (str code "\n")
                  (with-out-str (pprint code))))
              codes)))

(defn one-liner?
  [s]
  (if s
    (< (count (remove empty? (str/split s #"\s*\n\s*"))) 2)
    true))

(defn code*
  "Show codes (literal strings or forms) in a pre/code block."
  [& codes]
  (let [code-string (apply format-code codes)
        class-string "brush: clojure; toolbar: false;"
        class-string (if (one-liner? code-string) (str class-string  " light: true;") class-string)]
    [:script {:type "syntaxhighlighter" :class class-string}
     (str "<![CDATA[" code-string "]]>")]))

(defn render
  "Render docstring and source for a var"
  [var sym]
  (println var sym)
  [:div
   [:h4 "Docstring"]
   [:pre [:code
          (with-out-str (print-doc var))]]
   [:h4 "Source"]
   (code* (clojure.repl/source-fn sym))])

