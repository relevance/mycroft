(ns mycroft.class
  (:use [clojure.tools.logging :only (info)]
        [mycroft.data :only (render-type)]
        mycroft.selector)
  (:require [mycroft.reflect :as reflect]
            [mycroft.breadcrumb :as breadcrumb]))

(defn render
  [cls n-params selection]
  (info "Class" cls)
  (info "Selection" selection)
  (let [obj (reflect/members cls)
        selectors (:selectors n-params)
        selection (select-in obj selectors)]
    [:div
     [:div {:id "breadcrumb"} (breadcrumb/render cls n-params selection)]
     [:div (render-type {:superclasses (supers cls)} {})]
     [:div (render-type selection
                        (if selectors
                          n-params
                          (assoc n-params :headers
                                 [:name :type :parameter-types :return-type :modifiers :declaring-class])))]]))
