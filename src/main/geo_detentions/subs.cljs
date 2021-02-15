(ns geo-detentions.subs
  (:require
   [re-frame.core :refer [reg-sub subscribe]]
   [re-posh.core :as rp])
  )

(rp/reg-sub
 :ovds
 (fn [_ _]
   {:type :query
    :query '[:find ?id ?name ?loc ?address
             ;; :keys :name :location :address :: TODO: figure out why doesn't work  https://github.com/denistakeda/posh/issues/10
             :where [?e :ovd/id ?id]
             [?e :ovd/name ?name]
             [?e :ovd/location ?loc]
             [?e :ovd/address ?address]]
    }))

(rp/reg-sub
 :selected-ovd
 (fn [_ _]
   {:type :query
    :query '[:find ?id
             :where [?e :selected-ovd ?id]]}))


(rp/reg-sub
 :selected-ovd-event-ids
 (fn [_ _]
   {:type :query
    :query '[:find ?e
             :where [_ :selected-ovd ?ovd-id]
             [?o :ovd/id ?ovd-id]
             [?e :event/ovd ?o]]}))

(rp/reg-sub
 :selected-ovd-events
 :<- [:selected-ovd-event-ids]
 (fn [entity-ids _]
   {:type    :pull-many
    :pattern '[*]
    :ids      (reduce into [] entity-ids)}))

(comment

  (datascript/q '[:find
                  ?e
                  :in $ ?ovd-id
                  :where
                  [?o :ovd/id ?ovd-id]
                  [?e :event/ovd ?o]
                  [?e :event/event_title ?t]
                  [?e :event/detentions ?d]
                  ]
                @@rdb/store
                108
                ))


(defn <sub
  [c]
  (try
    (-> (subscribe c)
        (deref))
    (catch js/Error e
      (throw e))))
