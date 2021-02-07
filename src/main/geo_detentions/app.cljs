(ns geo-detentions.app
  (:require
   [geo-detentions.events :as events]
   [geo-detentions.views :as views]
   [geo-detentions.subs :as subs]
   [reagent.dom :as rdom]
   ))

(defn init! []
  (re-frame.core/dispatch-sync [:initialise-db]) ;; TODO: use just dispatch
  (rdom/render [views/main]  (.getElementById js/document "app")))

(comment
  )
