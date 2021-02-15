(ns geo-detentions.app
  (:require
   [geo-detentions.events :as events]
   [geo-detentions.views :as views]
   [geo-detentions.subs :as subs]
   [reagent.dom :as rdom]
   [re-posh.core :as rp]

   [re-posh.db :as rdb] ;; TODO: remove in prod
   [datascript.core :as ds]
   ))

(defn init! []
  (rp/dispatch-sync [:initialize-db]) ;; TODO: use just dispatch
  (rp/dispatch [:load-detentions])
  (rdom/render [views/main]  (.getElementById js/document "app")))

(comment

  ;; explore datascripyt
    (require '[re-posh.db :as rdb]
             '[datascript.core :as ds])

    (do
      (def c (ds/create-conn))
      )

  (ds/q '[:find ?e
           :in $ ?oid
           :where
           [?e :ovd/id ?oid]
           [?e :ovd/name ?name]
           [?e :ovd/address ?addr]
           [?e :ovd/location ?loc]
           ] @rdb/store
          101
          )

  (ds/q '[:find ?reg
          :where [_ :region ?reg]]
   @rdb/store
   )
  )
