(ns geo-detentions.views
  (:require
   [reagent.core :as r]
   [re-posh.core :as rp]
   [goog.object]
   [geo-detentions.subs :refer [<sub]]))

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
              #(let [ovd-id (goog.object.getValueByKeys % "target" "ovdId")]
                 (rp/dispatch [:set-filter :filter/ovd  ovd-id]))]

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
  (let [ovds (<sub [:ovds])]
    (fn []
      [map-inner nil ovds])))

(defn detentions-table []
  (when-let [events (<sub [:selected-events])]
    [:table.table.content
     [:thead
      [:tr
       [:th "id"]
       [:th "Название"]
       [:th "Место задержания"]
       [:th "Количество задержанных"]
       [:th "Дата"]
       [:th "Описание"]
       [:th "Согласовано"]
       [:th "event type"]
       [:th "subject type"]
       [:th "subject topic"]
       [:th "subject story"]
       [:th "Тип организатора"]
       [:th "Организатор"]
       [:th "Ссылки"]
       ]]

     [:tbody
      (for [e events]
        ^{:key (:event/event_id e)}
        [:tr
         [:td (:event/event_id e)]
         [:td (:event/event_title e)]
         [:td (:event/place e)]
         [:td (:event/detentions e)]
         [:td (:event/date e)]
         ;; [:td (:event/region e)]
         [:td (:event/description e)]
         [:td (-> e :event/agreement :agreement)]
         [:td (-> e :event/event_type :event_type)]
         [:td (:event/subject_type e)]
         [:td (:event/subjec_topic e)]
         [:td (:event/subjec_story e)]
         [:td (-> e :event/organizer_type :organizer_type)]
         [:td (:event/organizer_name e)]
         [:td (:event/links e)]
         ])]]))

(defn filters []
  [:div.navbar
   (let [filters (<sub [:filter])]
     (prn "IN VIEW FILTER IS " filters)

     [:div.navbar-start
      [:div.navbar-item
       [:div.field
        [:label.label "Дата мин."]
        [:div.control
         [:input
          {:type :date
           :name :date-from
           :min "2013-01-01"
           :value (:filter/date-from filters)
           :on-change
           #(let [v (goog.object/getValueByKeys % "target" "value")]
              (rp/dispatch [:set-filter :filter/date-from v]))
           }]]]]

      [:div.navbar-item
       [:div.field
        [:label.label "Дата макс."]
        [:div.control
         [:input
          {:type :date
           :name :date-till
           :max "2022-01-01"
           :value (:filter/date-till filters)
           :on-change
           #(let [v (goog.object/getValueByKeys % "target" "value")]
              (rp/dispatch [:set-filter :filter/date-till v]))
           }]]]]

      ])])


(defn main []
  [:div
   [map-outer]
   [filters]
   [detentions-table]])
