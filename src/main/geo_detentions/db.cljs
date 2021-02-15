(ns geo-detentions.db
  (:require
   [datascript.core    :as d]
   [re-posh.core       :as rp]
   [geo-detentions.ovds :refer [ovds]]
   ))

(def initial-db
  (concat
   ovds
   [{:region "Москва"}
     {:region "Санкт-Петербург"}
     {:agreement "-"}
     {:agreement "согласовано"}
     {:event_type "одиночный пикет"}
     {:event_type "акция"}
     {:event_type "собрание"}
     {:event_type "оккупай"}
     {:event_type "в движении"}
     {:event_type "культурно-просветительское мероприятие"}
     {:event_type "агитация"}
     {:organizer_type "активистский протест"}
     {:organizer_type "институциональный протест"}
     {:organizer_type "стихийный протест"}]
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
