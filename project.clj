(defproject mycroft "0.0.2"
  :description "It's your data"
  :dependencies [[org.clojure/clojure "1.2.0"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [compojure "0.4.1"]
                 [ring/ring-jetty-adapter "0.2.5"]
                 [hiccup "0.2.6"]]
  :dev-dependencies [[autodoc "0.7.0"]
                     [jline "0.9.94"]
                     [swank-clojure "1.3.0-SNAPSHOT"]]
  :repositories {"clojure-releases" "http://build.clojure.org/releases"})
