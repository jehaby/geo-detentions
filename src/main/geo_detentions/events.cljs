(ns geo-detentions.events
  (:require
   [re-frame.core :refer [reg-event-db debug trim-v]]
   ;; [geo-detentions.ovds :refer [ovds]]
   [geo-detentions.db :as db]
   [re-posh.core :as rp]
   ["./data.js" :refer [detensions]]
   ))

(def default-interceptors [debug trim-v])

(rp/reg-event-ds
 :initialize-db
 default-interceptors
 (fn [_ _]
   db/initial-db))

(def selected-ovd-id 141666)

(rp/reg-event-ds
 :select-ovd
 default-interceptors
 (fn [_ [id]]
    [[:db/add selected-ovd-id :selected-ovd id]]))
