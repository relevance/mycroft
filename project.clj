(defproject mycroft "0.0.2"
  :description "It's your data"
  :dependencies [[org.clojure/clojure "1.3.0-alpha2"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [compojure "0.5.2"]
                 [ring/ring-jetty-adapter "0.3.2"]
                 [hiccup "0.3.0"]]
  :dev-dependencies [[autodoc "0.7.0"]
                     [jline "0.9.94"]
                     [swank-clojure "1.3.0-SNAPSHOT"]]
  :repositories {"clojure-releases" "http://build.clojure.org/releases"})
