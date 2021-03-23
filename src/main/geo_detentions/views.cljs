(ns geo-detentions.views
  (:require
   [reagent.core :as r]
   [re-posh.core :as rp]
   [goog.object]
   [geo-detentions.subs :refer [<sub]]
   [geo-detentions.db :as db]
   [react-select :default Select]
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

(defn sortable-table-header
  [{sort-field :sort/field asc? :sort/asc?} field label]
  (let [cur-sort-field? (= sort-field field)
        sort-sym (if asc? " ↘" " ↗")]
    [:th
     {:on-click
      #(let [new-asc? (if cur-sort-field? (not asc?) true)]
         (rp/dispatch [:set-sort field new-asc?]))}
     (str label (when cur-sort-field? sort-sym))]))

(defn selectize-options
  [m]
  ;; transforms map of {label value} pairs to seq of maps {:label label :value value}}
  (for [[label v] m]
    {:label label :value (name v)}))

(defn selectize-values
  [k vals]
  (let [lv (select-keys db/enums->vals vals)]
    (for [[v label] lv]
      {:label label :value (name v)})))

(defn react-select
  [placeholder options val on-change]
  [:> Select
   {:isMulti true
    :options options
    :placeholder placeholder
    :value val
    :on-change on-change
    }])

(defn detentions-table []
  (let [events (<sub [:sorted-events])
        sorting (<sub [:sorting])]
    [:table.table.content
     [:thead
      [:tr
       [:th "id"]
       [:th "Название"]
       [:th "Место задержания"]
       (sortable-table-header sorting :event/detentions "Количество задержанных")
       (sortable-table-header sorting :event/date "Дата")
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
         [:td (:event/description e)]
         [:td (-> e :event/agreement db/enums->vals)]
         [:td (-> e :event/event_type db/enums->vals)]
         [:td (:event/subject_type e)]
         [:td (:event/subject_topic e)]
         [:td (:event/subject_story e)]
         [:td (-> e :event/organizer_type db/enums->vals)]
         [:td (:event/organizer_name e)]
         [:td (:event/links e)]
         ])]]))

(defn filters []
  [:div.navbar
   (let [filters (<sub [:filter])]
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

      [:div.navbar-item
       [:div.field
        [:label.label "Тип события"]
        [:div.control
         [react-select
          "Тип события"
          (selectize-options (:event/event_type db/vals->keywords))
          (selectize-values "event_type" (:filter/event_types filters))
          #(let [v (map
                    (fn [m] (->> (get m "value")
                                 (keyword "event_type")))
                    (js->clj %))]
             (rp/dispatch [:set-filter :filter/event_types v])
             )]
         ]]]
      ])])

(defn main []
  [:div
   [map-outer]
   [filters]
   [detentions-table]])
