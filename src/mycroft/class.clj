(ns mycroft.class
  (:use [clojure.tools.logging :only (info)]
        [mycroft.data :only (render-type)]
        mycroft.selector)
  (:require [clojure.reflect :as reflect]
            [mycroft.breadcrumb :as breadcrumb]))

(defn render
  [cls n-params selection]
  (info "Class" cls)
  (info "Selection" selection)
  (let [obj (reflect/reflect cls)
        selectors (:selectors n-params)
        selection (select-in obj selectors)
        n-params (if (= (last selectors) :members)
                   (assoc n-params :headers [:name :type :parameter-types :return-type :modifiers :declaring-class :exception-types :flags])
                   n-params)]
    [:div
     [:div {:id "breadcrumb"} (breadcrumb/render cls n-params selection)]
     [:div (render-type selection n-params)]]))
