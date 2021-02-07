(ns geo-detentions.subs
  (:require [re-frame.core :refer [reg-sub subscribe]])
  )

(reg-sub
 :map-data
 (fn [db _]
   (:map-data db)))

(defn <sub
  [c]
  (try
    (-> (subscribe c)
        (deref))
    (catch js/Error e
      (throw e))))
