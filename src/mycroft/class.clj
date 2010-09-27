(ns mycroft.class
  (:use [mycroft.data :only (select-in render-type)])
  (:require [mycroft.reflect :as reflect]
            [mycroft.breadcrumb :as breadcrumb]))

(defn render
  [classname options]
  (let [cls (Class/forName classname)
        obj (reflect/members cls)
        selectors (:selectors options)
        selection (select-in obj selectors)]
    [:div
     [:div {:id "breadcrumb"}
      (breadcrumb/top-link)
      [:span " &laquo; "
       (str "class " classname)]]
     [:div
      (render-type {:superclasses (supers cls)}
                   {})]
     [:div
      (render-type selection
                   (if selectors
                     options
                     (assoc options :headers
                            [:name :type :parameter-types :return-type :modifiers :declaring-class])))]]))
