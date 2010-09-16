(ns zap.history)

(def history (atom (list)))

(defn update-history
  [request]
  (swap! history #(->> % (cons request) (take 5))))

(defn with-recent-history [handler]
  (fn [request]
    (-> request update-history handler)))




