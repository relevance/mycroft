(defproject mycroft "0.0.3-SNAPSHOT"
  :description "It's your data"
  :main mycroft.main
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/tools.logging "0.1.2"]
                 [org.clojure/java.jmx "0.1"]
                 [ring/ring-jetty-adapter "0.3.8"]
                 [compojure "0.6.3" :exclusions [org.clojure/clojure]]
                 [hiccup "0.3.5" :exclusions [org.clojure/clojure]]]
  :repositories {"sonatype-oss-public" "https://oss.sonatype.org/content/groups/public/"})
