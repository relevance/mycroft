(ns mycroft.class
  (:use [mycroft.data :only (select render-type)])
  (:require [mycroft.reflect :as reflect]
            [mycroft.breadcrumb :as breadcrumb]))

(defn render
  [classname options]
  (let [cls (Class/forName classname)
        obj (reflect/reflect cls)
        selector (:selector options)
        selection (select obj selector)]
    [:div
     [:div {:id "breadcrumb"}
      (breadcrumb/top-link)
      [:span " &laquo; "
       (str "class " classname)]]
     [:div
      (render-type selection options)]]))
