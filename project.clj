(defproject mycroft "0.0.3-SNAPSHOT"
  :description "It's your data"
  :dependencies [[org.clojure/clojure "1.5.0-master-SNAPSHOT"]
                 [org.clojure/tools.logging "0.2.3"]
                 [org.clojure/java.jmx "0.2.0"]
                 [ring/ring-jetty-adapter "1.1.6"]
                 [compojure "1.1.3" :exclusions [org.clojure/clojure]]
                 [hiccup "1.0.2" :exclusions [org.clojure/clojure]]]
  :dev-dependencies [[swank-clojure "1.3.0-SNAPSHOT"
                      :exclusions [org.clojure/clojure]]]
  :repositories {"sonatype-oss-public" "https://oss.sonatype.org/content/groups/public/"})
