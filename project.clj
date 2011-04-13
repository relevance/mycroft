(defproject mycroft "0.0.3-SNAPSHOT"
  :description "It's your data"
  :dependencies [[org.clojure/clojure "1.3.0-master-SNAPSHOT"]
                 [org.clojure/tools.logging "0.1.2"]
                 [org.clojure/java.jmx "0.1"]
                 [ring/ring-jetty-adapter "0.3.7"]
                 [compojure "0.6.2"]
                 [hiccup "0.3.4"]]
  :dev-dependencies [[swank-clojure "1.3.0-SNAPSHOT" :exclusions [org.clojure/clojure]]])
