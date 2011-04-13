(ns mycroft.server
  (:use [ring.adapter.jetty :only (run-jetty)]
        [ring.util.response :only (redirect)]
        [ring.middleware.params :only (wrap-params)]
        [ring.middleware.keyword-params :only (wrap-keyword-params)]
        [ring.middleware.cookies :only (wrap-cookies)]
        [compojure.core :only (defroutes GET POST routes)]
        [clojure.pprint :only (pprint)]
        [clojure.java.browse :only (browse-url)])
  (:require [compojure.route :as route]
            [mycroft.layouts.application :as layout]
            [mycroft.handlers :as handlers]
            [mycroft.breadcrumb :as breadcrumb]
            [mycroft.history :as history]
            [mycroft.examples :as examples]))

(defroutes base-routes
  (GET "/vars" [] (layout/namespaces))
  (GET "/foo" request (prn request))
  (GET "/vars/*" {:keys [params]} (layout/vars params))
  (GET "/classes/*" {:keys [params]} (layout/classes params))
  (route/files "/")
  (route/not-found "not found"))

(def application (-> base-routes
                     examples/with-recent-history
                     (handlers/wrap-logging)
                     wrap-keyword-params
                     wrap-params
                     wrap-cookies))

(defprotocol Inspector
  (launch [_])
  (inspect [_ obj options]))

(defrecord Instance [port]
  Inspector
  (launch [_]
    (run-jetty (var application) {:port port
                                  :join? false}))
  (inspect [_ obj options]
    (let [query (breadcrumb/params->query-string options)]
      (if (class? obj)
        (browse-url (str "http://localhost:" port
                         "/classes/" (.getName obj) "?"
                         query))
        (browse-url (str "http://localhost:" port
                         (history/add obj) "&"
                         query))))))
