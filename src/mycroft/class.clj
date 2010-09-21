(ns mycroft.class
  (:use [mycroft.data :only (select-in render-type)])
  (:require [mycroft.reflect :as reflect]
            [mycroft.breadcrumb :as breadcrumb]))

(defmulti customize-options
  (fn [options selectors] selectors))

(defmethod customize-options :default [options & _] options)

(defmethod customize-options [:fields]
  [options _]
  (assoc options :headers [:name :type :modifiers]))

(defmethod customize-options [:methods]
  [options _]
  (assoc options :headers [:name :parameter-types :return-type :modifiers]))

(defmethod customize-options [:constructors]
  [options _]
  (assoc options :headers [:name :parameter-types :modifiers]))

(defn render
  [classname options]
  (let [cls (Class/forName classname)
        obj (reflect/reflect cls)
        selectors (:selectors options)
        selection (select-in obj selectors)]
    [:div
     [:div {:id "breadcrumb"}
      (breadcrumb/top-link)
      [:span " &laquo; "
       (str "class " classname)]]
     [:div
      (render-type selection (customize-options options selectors))]]))
