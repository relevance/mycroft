(ns mycroft.server
  (:use [ring.adapter.jetty :only (run-jetty)]
        [ring.util.response :only (redirect)]
        [compojure.core :only (defroutes GET POST routes)]
        [hiccup.core :only (html)]
        [hiccup.page-helpers :only [include-js include-css]]
        [clojure.pprint :only (pprint)]
        [clojure.walk :only (keywordize-keys)]
        [clojure.java.browse :only (browse-url)])
  (:require
   [compojure.route :as route]
   [mycroft.jmx :as jmx]
   [mycroft.resources :as resources]
   [mycroft.data :as data]
   [mycroft.class :as class]
   [mycroft.namespace :as namespace]
   [mycroft.docs :as docs]
   [mycroft.history :as history]
   [clojure.string :as str]
   [mycroft.examples :as examples]))

(defn minib-layout [title & body]
  (html
    [:head
     [:title title]
     (include-css "/stylesheets/shCore.css"
                  "/stylesheets/shThemeDefault.css")
                  [:link {:type "text/css", :href "/stylesheets/mobile.css", :rel "stylesheet", :media ""}]
     [:meta {:name "viewport" :content "user-scalable=no, width=device-width"}]
     (include-js "/javascripts/jquery.1.3.2.min.js"
                 "/javascripts/application.js"
                 "/javascripts/shCore.js"
                 "/javascripts/shBrushClojure.js")]
    [:body {:id "browser"}
     [:h2 {:class "logo"} [:a {:href "/" :class "home"} "Home"] [:a {:href "/index.html"} "Mycroft, a Clojure inspector"]]
     [:div {:id "content"}
      body]
     [:div {:id "footer"}
      "Clojure Mini-Browser"]]))

(defn with-logging [handler]
  (fn [request]
    (let [start (System/nanoTime)
          response (handler request)
          elapsed (/ (double (- (System/nanoTime) start)) 1000000.0)]
      (when response
        (println (str (:uri request) " [" (:request-method request) "] " elapsed " msec"
                      "\n\tParameters " (:params request)
                      "\n\tSession " (:session request)))
        response))))

(defn parse-start
  [x]
  (try
   (max (Integer/parseInt x) 0)
   (catch NumberFormatException e nil)))

(defn normalize-options
  "Convert options from string form (as coming in from web)
   to data structures as needed."
  [options]
  (let [options (keywordize-keys options)
        options (if (:selector options)
                  (update-in options [:selector] read-string)
                  options)
        options (if (:start options)
                  (update-in options [:start] parse-start)
                  options)
        options (merge {:start 0} options)]
    options))


(defroutes namespace-routes
  (GET "/vars" []
       (html
        (minib-layout
         "Namespaces"
         (namespace/browser)))))

(defroutes class-routes
  (GET "/classes/*"
       {:keys [params query-params]}
       (let [classname (get params "*")]
         (html
          (minib-layout
           classname
           (class/render classname (normalize-options query-params)))))))

(defroutes var-routes
  (GET
   "/vars/*"
   {:keys [params query-params]}
   (let [qname (get params "*")
         [ns var] (str/split qname #"/")]
     (namespace/safe-load-ns ns)
     (html
      (minib-layout
       qname
       (if var
         (data/render (find-var (symbol qname)) (normalize-options query-params))
         (namespace/var-browser ns)))))))

(def dynamic-routes (routes (-> namespace-routes examples/with-recent-history)
                            var-routes
                            class-routes))

(defroutes static-routes
  (resources/resources "/" {:root "/public"})
  (route/not-found "not found"))

(defroutes app
  (routes dynamic-routes
          #_(-> dynamic-routes with-logging)
          static-routes))

(defprotocol Inspector
  (launch [_])
  (inspect [_ obj]))

(defrecord Instance [port]
  Inspector
  (launch [_]
          (run-jetty (var app) {:port port
                                :join? false}))
  (inspect [_ obj]
           (if (class? obj)
             (browse-url (str "http://localhost:" port
                              "/classes/" (.getName obj)))
             (browse-url (str "http://localhost:" port
                              (history/add obj))))))