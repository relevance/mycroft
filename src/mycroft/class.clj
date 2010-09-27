(ns mycroft.class
  (:use [mycroft.data :only (render-type)]
        mycroft.selector)
  (:require [mycroft.reflect :as reflect]
            [mycroft.breadcrumb :as breadcrumb]))

(defn render
  [classname options selection]
  (let [cls (when classname (Class/forName classname))
        obj (reflect/members cls)
        selectors (:selectors options)]
    [:div
     [:div {:id "breadcrumb"}
      (breadcrumb/render cls options)]
     [:div
      (render-type {:superclasses (supers cls)}
                   {})]
     [:div
      (render-type selection
                   (if selectors
                     options
                     (assoc options :headers
                            [:name :type :parameter-types :return-type :modifiers :declaring-class])))]]))
