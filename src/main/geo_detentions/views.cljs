(ns geo-detentions.views
  (:require
   [reagent.core :as r]
   [re-posh.core :as rp]
   [goog.object]
   [re-frame.core :refer [subscribe dispatch]]
   ))

(defn map-inner [data ovds]
  (let [state (atom nil)
        tile-url "https://api.mapbox.com/styles/v1/{id}/tiles/{z}/{x}/{y}?access_token={accessToken}"
        tile-settings #js{
                          "attribution" "Map data &copy; <a href=\"https://www.openstreetmap.org/copyright\">OpenStreetMap</a> contributors, Imagery © <a href=\"https://www.mapbox.com/\">Mapbox</a>"
                          "maxZoom" 18,
                          "id" "mapbox/streets-v11",
                          "tileSize" 512,
                          "zoomOffset" -1,
                          "accessToken" "pk.eyJ1IjoiamVoYWJ5IiwiYSI6ImNra2Q2cGk3ZzBvYjMydW9jcHk1a3RlcXAifQ.-44DkUzBK29KgDZyTCV2HQ"}
        map-center (array 55.751167, 37.620716)] ;; TODO from params
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

              ovd-click-handler
              #(rp/dispatch [:select-ovd (goog.object.getValueByKeys % "target" "ovdId")])]

          (doseq [[id name loc addr] ovds]
            (let [loc (array (:lat loc) (:lon loc))
                  marker (.circleMarker js/L loc)]
              (set! (.-ovdId marker) id)
              (-> marker
                  (.addTo m)
                  (.on "click" ovd-click-handler))))
          (reset! state {:map m})
          ))

      ;; :component-did-update
      ;; (fn [comp]
      ;;   (let [props (r/props comp)
      ;;         [lat lng] (:marker-pos props)
      ;;         latLng (js/L.latLng. lat lng)]
      ;;     (.setLatLng (:marker @state) latLng)))

      :display-name "map-inner"}
     )))

(defn map-outer []
  (let [;; data (subscribe [:map-data])
        ovds (rp/subscribe [:ovds])]
    (fn []
      [map-inner nil @ovds])))

(defn main []
  [:div
   [map-outer]
   [:div
    [:button {:on-click #(dispatch [:change-c])} "inc"]
    (let [
          ;; data @(subscribe [:map-data])
          ovds (rp/subscribe [:ovds])
          su (rp/subscribe [:selected-ovd])
          f (first @ovds)]
      ;; [:h1 (str (:marker-pos data))]
      [:h1 "OVD: "
       [:p [:string (str @su)]]
       [:p (str f)]]
      )]])
