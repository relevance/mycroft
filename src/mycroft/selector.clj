(ns mycroft.selector)

(defn special-selector?
  "Keywords in the mycroft namespace are interpreted specially,
   instead of just drilling further into the collection by index or
   key."
  [selector]
  (and (keyword? selector)
       (= (namespace selector) "mycroft")))

(defn add-selector
  "Update the options by adding a selector to the end of the 
   selectors already included."
  [options s]
  (let [options (if (:selectors options)
                  (update-in options [:selectors] conj s)
                  (assoc options :selectors [s]))
        options (if (special-selector? s)
                  options
                  (dissoc options :start))]
    (dissoc options :headers)))

(defn select
  [item sel]
  (cond
   (= sel :mycroft/deref) @item
   (= sel :mycroft/meta) (meta item)
   (associative? item) (get item sel)
   (set? item) (nth (seq item) sel)
   (integer? sel) (nth item sel)))

(defn select-in
  "Like get-in on steroids.

   * basic get-in behavior, plus
   * uses nth to follow (in O(n) time!) lazy sequences.
   * follows magic key mycroft./meta to metadata
   * follows mycroft./deref to indirect through reference"
  [item selectors]
  (reduce select
   item
   selectors))