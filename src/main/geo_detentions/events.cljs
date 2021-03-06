(ns geo-detentions.events
  (:require
   [re-frame.core :refer [trim-v debug]]
   ;; [geo-detentions.ovds :refer [ovds]]
   [goog.labs.format.csv :as csv]
   [geo-detentions.db :as db]
   [geo-detentions.config :as cfg]
   [ajax.core :refer [raw-response-format]]
   [day8.re-frame.http-fx]
   [re-posh.core :as rp]
   [datascript.core :as datascript]

   [re-posh.db :as rdb] ;; TODO: remove in prod
   ))

(def default-interceptors [trim-v debug])

(rp/reg-event-fx
 :initialize-db
 default-interceptors
 (fn [_ _]
   {:transact db/initial-db}))

(rp/reg-event-ds
 :set-filter
 default-interceptors
 (fn [_ [filter-name value]]
   (concat
    [[:db/retract db/filter-entity-id filter-name]]
    (for [v (if (sequential? value) value [value])]
     [:db/add db/filter-entity-id filter-name v]))))

(rp/reg-event-ds
 :set-sort
 default-interceptors
 (fn [_ [field asc?]]
   [[:db/add db/sort-entity-id :sort/field field]
    [:db/add db/sort-entity-id :sort/asc? asc?]]))

(rp/reg-event-fx
 :load-detentions
 default-interceptors
 (fn [_ _]
   {:http-xhrio {:method :get
                 :uri (str cfg/APP-PREFIX "/data/data_2017_2019.csv")
                 :timeout 5000
                 :response-format (raw-response-format)
                 :on-success [:load-detentions-success]
                 :on-failure [:load-detentions-failure]}}))

(defn csv-data->maps [csv-data key-ns]
  (map zipmap
       (->> (first csv-data) ;; First row is the header
            (map (partial keyword key-ns))
            repeat)
	     (rest csv-data)))

(defn- attr-map
  "Returns map of `{attr-value attr-id}` pairs for given attribute."
  [conn attr]
  (->> (datascript/q `[:find ?val ?e :where [?e ~attr ?val]] conn)
       (into {})))

(defn str->int [i] (js/parseInt i))

(defn get-msk-ovd-id [conn val]
  (datascript/q '[:find ?e
                  :in $ ?name
                  :where [?e :ovd/name ?n]
                  [(clojure.string/includes? ?n ?name)]]
                conn val))

(defn make-ovd-coercer [conn]
  (fn [val]
    (if (.includes val " ") val
        (or (-> (get-msk-ovd-id conn val) ffirst) val))))

(defn coercion-fns
  "Returns map of key-fn for coercion values of event csv row"
  [conn]
  (let [val->keyword (fn [field val] (get-in db/vals->keywords [field val]))]
    {:event/detentions str->int
     :event/event_id str->int
     :event/year str->int
     :event/ovd (make-ovd-coercer conn)
     :event/agreement (partial val->keyword :event/agreement)
     :event/event_type (partial val->keyword :event/event_type)
     :event/region (partial val->keyword :event/region)
     :event/organizer_type (partial val->keyword :event/organizer_type)
     }))

(defn to-tx-data
  "Returns data prepared for insertion into datascript db"
  [coercion-fns csv-keys csv-row]
  (reduce
   (fn [m i]
     (let [k (get csv-keys i)
           csv-val (get csv-row i)
           coercion-fn (get coercion-fns k identity)]
       (assoc m k (coercion-fn csv-val))))
   {}
   (range (count csv-keys))))

(rp/reg-event-fx
 :load-detentions-success
 [(rp/inject-cofx :ds)]
 (fn [{:keys [ds]} [_ resp]]
   (let [parsed (csv/parse resp)
         csv-keys (->> (first parsed)
                       (map (partial keyword "event"))
                       (into []))
         c-fns (coercion-fns ds)]
     {:transact (map
                 #(to-tx-data c-fns csv-keys %)
                 (rest parsed))})))

(rp/reg-event-fx
 :load-detentions-failure
 ;; default-interceptors
 (fn [_ [resp]]
   (prn "IN  :load-detentions-failure" (type resp) (resp))
   ;;  TODO implement
   ))


(comment
  (datascript/q '[:find ?e ?ovd
                  ;; ?title ?region
                  ;; ?date ?det ?desc ?ovd ?place
                  :where
                  ;; [?e :event/event_id 959]
                  [?e :event/event_title ?title]
                  [?e :event/date ?date]
                  [?e :event/region ?region]
                  [?e :event/detentions ?det]
                  [?e :event/description ?desc]
                  [?e :event/ovd ?ovd]
                  [?e :event/place ?place]
                  [?e :event/region 68]
                  [?o :ovd/name ?ovd-name]
                  ;; [(= )]
                  [(< 10 ?det)]
                  [(< ?det 20)]
                  ;; [?o :ovd/]
                  ;; [(< ?det 200)]
                  ]
                @@rdb/store)


  ;;"Пресненский"
  ;; Отдел полиции №28 Центрального района г.Санкт-Петербурга


  (datascript/q '[:find ?e ?id
                  :in $ ?name
                  :where [?e :ovd/name ?n]
                  [?e :ovd/id ?id]
                  [(clojure.string/includes? ?n ?name)]
                  ]
                @@rdb/store
                "Пресненский")

  (def rrr (datascript/q '[:find
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


  (datascript/pull @@rdb/store '[:event/event_title
                                 {:event/ovd [:ovd/name]}
                                 {:event/agreement (:agreement)}
                                 ] 142880)

  (datascript/pull @@rdb/store '[:event/event_title] 142880)


  (datascript/q '[:find ?name
                  :where
                  ;; [24 :ovd/id 24]
                  [2 :ovd/name ?name]
                  ;; [?e :]
                  ]
                @@rdb/store)


  (->
   (datascript/pull @@rdb/store '[*] db/filter-entity-id)
   :filter/event_types
   ;; clj->js
   )


  (->
   (datascript/pull @@rdb/store '[*] 144050)
   ;; :filter/event_types
   ;; clj->js
   )

  (datascript/q '[:find ?e :where [?e :event/event_id 1285]] @@rdb/store)




  (def c (datascript/create-conn {:aka {:db/cardinality :db.cardinality/many}}))



  (datascript/q '[:find ?e ?n ?rn
                  :where [?e :event/event_id 1000]
                  [?e :event/event_type ?et]
                  [?e :event/region ?r]
                  [_ ?et ?n]
                  [_ ?r ?rn]]
                @@rdb/store)

  (datascript/q '[:find ?n
                  :where
                  [_ ?et ?n]
                  [_ :filter/event_types ?et]
                  ;; [?e :event/event_type ?et]
                  ;; [?e :event/region ?r]
                  ;; [_ ?et ?n]
                  ;; [_ ?r ?rn]
                  ]
                @@rdb/store)

  )
