(ns geo-detentions.views
  (:require
   [reagent.core :as r]
   [re-frame.core :refer [subscribe dispatch]]
   ))

(defn map-inner [data]
  (let [state (atom nil)
        tile-url "https://api.mapbox.com/styles/v1/{id}/tiles/{z}/{x}/{y}?access_token={accessToken}"
        tile-settings #js{
                          "attribution" "Map data &copy; <a href=\"https://www.openstreetmap.org/copyright\">OpenStreetMap</a> contributors, Imagery © <a href=\"https://www.mapbox.com/\">Mapbox</a>"
                          "maxZoom" 18,
                          "id" "mapbox/streets-v11",
                          "tileSize" 512,
                          "zoomOffset" -1,
                          "accessToken" "pk.eyJ1IjoiamVoYWJ5IiwiYSI6ImNra2Q2cGk3ZzBvYjMydW9jcHk1a3RlcXAifQ.-44DkUzBK29KgDZyTCV2HQ"}
        map-center (apply array (:marker-pos data))]
    (r/create-class
     {:reagent-render
      (fn []
        [:div
         [:h4 "Map"]
         [:div#map-canvas {:style {:height "600px" :width "90%"}}]])

      :component-did-mount
      (fn []
        (let [m (-> js/L (.map "map-canvas") (.setView map-center 13))
              tiles (-> js/L
                        (.tileLayer tile-url tile-settings)
                        (.addTo m))
              marker (-> js/L (.marker map-center) (.addTo m))]
          (reset! state {:map m :marker marker})))

      :component-did-update
      (fn [comp]
        (let [args (rest (r/argv comp))]
          (prn "Component did updated: "  args)
          )
        ;; (let [{:keys [_ ]} (r/props comp)
        ;;       latLng (js/L.latLng. latitude longitude)
        ;;       ]
        ;;   (.setLatLng (:marker @state) latLng)
        ;;   ;; (.panTo (:map @gmap) latlng)
        ;;   )
        )

      :display-name "map-inner"}
     )))

(defn map-outer []
  (let [data (subscribe [:map-data])]
    (fn []
      (prn "DATA IS " @data (type data))
      (map-inner @data)
      )
    ))

(defn main []
  [:div
   [map-outer]
   [:div
    [:button {:on-click #(dispatch [:change-c])} "inc"]
    (let [data @(subscribe [:map-data])]
      [:h1 (str (:marker-pos data))]
      )
    ]]
  )
