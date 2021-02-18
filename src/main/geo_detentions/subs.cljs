(ns geo-detentions.subs
  (:require
   [geo-detentions.db :as db]
   [re-frame.core :as re-frame]
   [re-posh.core :as rp]))

(rp/reg-sub
 :ovds
 (fn [_ _]
   {:type :query
    :query '[:find ?id ?name ?loc ?address
             ;; :keys :name :location :address :: TODO: figure out why doesn't work  https://github.com/denistakeda/posh/issues/10
             :where [?e :ovd/id ?id]
             [?e :ovd/name ?name]
             [?e :ovd/location ?loc]
             [?e :ovd/address ?address]]}))

(rp/reg-sub
 :selected-ovd
 (fn [_ _]
   {:type :query
    :query '[:find ?id
             :where [?e :selected-ovd ?id]]}))

(rp/reg-sub
 :selected-event-ids
 (fn [_ _]
   {:type :query
    :query '[:find ?e
             :where [_ :filter/ovd ?ovd-id]
             [?o :ovd/id ?ovd-id]
             [?e :event/ovd ?o]

             [?e :event/date ?date]
             [_ :filter/date-from ?date-from]
             [_ :filter/date-till ?date-till]
             [(< ?date ?date-till)]
             [(< ?date-from ?date)]]}))

(rp/reg-sub
 :selected-events
 :<- [:selected-event-ids]
 (fn [entity-ids _]
   {:type    :pull-many
    :pattern '[*]
    :ids      (reduce into [] entity-ids)}))

(rp/reg-sub
 :sorting
 (fn [_ _]
   {:type :pull
    :pattern '[*]
    :id db/sort-entity-id}))

(re-frame/reg-sub
 :sorted-events
 :<- [:selected-events]
 :<- [:sorting]
 (fn [[events sorting] _]
   (let [comparator (if (:sort/asc? sorting) < >)
         field (:sort/field sorting)]
     (sort-by field comparator events))))

(rp/reg-sub
 :filter
 (fn [_ _]
   {:type :pull
    :pattern '[*]
    :id db/filter-entity-id}))

(defn <sub
  [c]
  (try
    (-> (rp/subscribe c)
        (deref))
    (catch js/Error e
      (throw e))))
