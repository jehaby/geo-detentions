(ns geo-detentions.db
  (:require
   [datascript.core    :as d]
   [re-posh.core       :as rp]
   [geo-detentions.ovds :refer [ovds]]
   ))

(def filter-entity-id 142666)
(def sort-entity-id 143666)

(def filters [{:db/id filter-entity-id
               :filter/date-from "2013-01-01"
               :filter/date-till "2021-12-31"}])

(def sorting [{:db/id sort-entity-id
               :sort/field :event/date
               :sort/asc? true}])

(def vals->keywords
  {:event/region
   {"Москва" :region/moscow
    "Санкт-Петербург" :region/saint-petersburg}

   :event/agreement
   {"-" :agreement/na
    "согласовано" :agreement/agreed}

   :event/event_type
   {"одиночный пикет" :event_type/single_picket
    "акция" :event_type/action
    "собрание" :event_type/meeting
    "оккупай" :event_type/occupy
    "в движении" :event_type/afoot
    "культурно-просветительское мероприятие" :event_type/cultural
    "агитация" :event_type/agitation}

   :event/organizer_type
   {"активистский протест" :organizer_type/activist
    "институциональный протест" :organizer_type/institutional
    "стихийный протест" :organizer_type/spontaneous}})

(def enums->vals (->> (vals vals->keywords)
                      (map clojure.set/map-invert)
                      (into {})))

(def event-enums (->> (seq enums->vals)
                      (map (partial apply hash-map))))

(def initial-db
  (concat
   ovds
   filters
   sorting
   event-enums
   ;; [{:region "Москва"}
   ;;  {:region "Санкт-Петербург"}
   ;;  {:agreement "-"}
   ;;  {:agreement "согласовано"}
   ;;  {:event_type "одиночный пикет"}
   ;;  {:event_type "акция"}
   ;;  {:event_type "собрание"}
   ;;  {:event_type "оккупай"}
   ;;  {:event_type "в движении"}
   ;;  {:event_type "культурно-просветительское мероприятие"}
   ;;  {:event_type "агитация"}
   ;;  {:organizer_type "активистский протест"}
   ;;  {:organizer_type "институциональный протест"}
   ;;  {:organizer_type "стихийный протест"}]
   ))


(def conn (d/create-conn))
(rp/connect! conn)

(comment
  "
  (
    year int,
    event_id int,
    event_title text,
    date text,
    region text,
    description text,
    agreement text,
    event_type text,
    subject_type text,
    subject_topic text,
    subject_story text,
    organizer_type text,
    organizer_name text,
    detentions int,
    ovd text,
    place text,
    links text
    );
"
  )
