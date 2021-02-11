(ns geo-detentions.db
  (:require
   [datascript.core    :as d]
   [re-posh.core       :as rp]
   [geo-detentions.ovds :refer [ovds]]
   ))

(def initial-db
  ovds)

(def conn (d/create-conn))
(rp/connect! conn)


