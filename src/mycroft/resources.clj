(ns mycroft.resources
  (:require ring.util.response)
  (:use [compojure.core :only (GET)]))

;; hacked from compojure and ring. Same code doesn't work in place in
;; ring, some sort of classpath issue. Also hacked / to /index.html,
;; should be done somewhere else.

(defn add-wildcard
  "Add a wildcard to the end of a route path."
  [path]
  (str path (if (.endsWith path "/") "*" "/*")))

(defn resource-response
  "Returns a Ring response to serve a packaged resource, or nil if the
  resource does not exist.
  Options:
    :root - take the resource relative to this root"
  [path & [opts]]
  (let [path   (if (= path "") "/index.html" path)
        path   (str (:root opts "") "/" path)
        path   (.replace path "//" "/")]
    (if-let [resource (.getResourceAsStream (class ring.util.response/file-response) path)]
      (ring.util.response/response resource))))

(defn resources
  "A route for serving resources from the classpath"
  [path & [options]]
  (GET (add-wildcard path) {{path "*"} :params}
       (resource-response path options)))

