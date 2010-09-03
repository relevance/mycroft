(defproject zap "0.0.1"
  :description "It's your data"
  :dependencies [
                 [org.clojure/clojure
                  "1.2.0"]
                 [org.clojure/clojure-contrib
                  "1.2.0"]
                 [compojure
                  "0.4.1"]
                 [ring/ring-jetty-adapter
                  "0.2.5"]
                 [hiccup
                  "0.2.6"]
                 [jline
                  "0.9.94"]
                 [circumspec
                  "0.0.10"]]
  :dev-dependencies [[autodoc "0.7.0"]]
  :repositories {"clojure-releases" "http://build.clojure.org/releases"})
