(ns geo-detentions.app
  (:require
   [geo-detentions.views :as views]
   [reagent.dom :as rdom]
   ))

(defn init! []
  (rdom/render [views/map-outer]  (.getElementById js/document "app")))

(comment
  )
