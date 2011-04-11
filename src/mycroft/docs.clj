(ns mycroft.docs
  (:use [clojure.pprint :only (pprint)]
        [clojure.tools.logging :only (info)])
  (:require [clojure.string :as str]
            [clojure.java.javadoc :as javadoc]
            clojure.repl))

(defn- format-code
  [& codes]
  (apply str (map
              (fn [code]
                (if (string? code)
                  (str code "\n")
                  (with-out-str (pprint code))))
              codes)))

(defn doc-string
  [v]
  (let [m (meta v)]
    (str (ns-name (:ns m))
         "/"
         (:name m)
         "\n"
         (:arglists m)
         "\n"
         (if (:macro m) "Macro\n" "")
         (:doc m))))

(defn- one-liner?
  [s]
  (if s
    (< (count (remove empty? (str/split s #"\s*\n\s*"))) 2)
    true))

(defn- code*
  "Show codes (literal strings or forms) in a pre/code block."
  [& codes]
  (let [code-string (apply format-code codes)
        class-string "brush: clojure; toolbar: false;"
        class-string (if (one-liner? code-string) (str class-string  " light: true;") class-string)]
    [:script {:type "syntaxhighlighter" :class class-string}
     (str "<![CDATA[" code-string "]]>")]))

(defn- var-symbol
  [v]
  (symbol (str (.ns v) "/" (.sym v))))

(defn render
  "Render docstring and source for a var"
  [var options]
  (info "Doc Var:" var)
  (info "Doc Options:" options)
  [:div
   [:h4 "Docstring"]
   [:pre [:code (doc-string var)]]
   [:h4 "Source"]
   (code* (clojure.repl/source-fn (var-symbol var)))])

(def javadoc-url @#'javadoc/javadoc-url)

(defn doc-url
  [o]
  (when o
    (if (class? o)
      (binding [javadoc/*feeling-lucky* false]
        (javadoc-url (.getName o)))
      (doc-url (class o)))))
