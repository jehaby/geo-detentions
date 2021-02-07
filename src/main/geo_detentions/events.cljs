(ns geo-detentions.events
  (:require
   [re-frame.core :refer [reg-event-db debug trim-v]]
   ))

(def default-interceptors [debug trim-v])

(reg-event-db
 :initialise-db
 default-interceptors
 (fn [_]
   {:map-data {:marker-pos [55.751167, 37.620716]}}
   ))

(reg-event-db
 :change-c
 default-interceptors
 (fn [db]
   (update-in db [:map-data :marker-pos 0] (partial + 0.001))))
