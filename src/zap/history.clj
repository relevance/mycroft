(ns zap.history)

(def history (atom (list)))

(defn append-to-history
  [request]
  (swap! history #(->> % (cons request) (take 5))))

(defn with-recent-history [handler]
  (fn [request]
    (let [response (handler request)]
      (when response
        (append-to-history request)
        response))))




