(ns geo-detentions.views
  (:require
   [reagent.core :as r]
   [re-frame.core :refer [subscribe dispatch]]
   ))


(defn map-inner [data]
  (let [leaflet-map (atom nil)
        tile-url "https://api.mapbox.com/styles/v1/{id}/tiles/{z}/{x}/{y}?access_token={accessToken}"
        tile-settings #js{
                          ;; "attribution": "Map data &copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors, Imagery © <a href="https://www.mapbox.com/">Mapbox</a>",
                          "attribution" "foooo"
                          "maxZoom" 18,
                          "id" "mapbox/streets-v11",
                          "tileSize" 512,
                          "zoomOffset" -1,
                          "accessToken" "pk.eyJ1IjoiamVoYWJ5IiwiYSI6ImNra2Q2cGk3ZzBvYjMydW9jcHk1a3RlcXAifQ.-44DkUzBK29KgDZyTCV2HQ"
                          }
        map-center (array 55.751167, 37.620716)]
  (r/create-class
   {:reagent-render (fn []
                      [:div
                       [:h4 "Map"]
                       [:div#map-canvas {:style {:height "400px" :width "90%"}}]])

    :component-did-mount (fn [comp]
                           (let [;; canvas  (.getElementById js/document "map-canvas")
                                 m (-> js/L (.map "map-canvas") (.setView map-center 10))
                                 tiles (-> js/L
                                           (.tileLayer tile-url tile-settings)
                                           (.addTo m))

                                 ]
                             (reset! leaflet-map {:map m}))
                           ;; (update comp)
                           )

    ;; :component-did-update update
    :display-name "map-inner"}
   )))


(defn map-outer []
  (let [data nil
        ;; (subscribe [:map-data])
        ]
    (fn []
      (map-inner data))))

