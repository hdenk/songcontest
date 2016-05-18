(ns songcontest.contest-form
  (:require-macros [cljs.core.async.macros :refer (go)])
  (:require
   [reagent.core :as reagent :refer [atom]]
   [reagent-forms.core :refer [bind-fields]]
   [cljs-http.client :as http]
   [cljs.core.async :refer (<!)]
   [schema.core :as s]))

(enable-console-print!)

(defonce doc (atom {:name "" :phase ""}))

(defn fetch-data! [id]
   (go (let [response
             (<! (http/get (str "/api/contest/" id)))
             data (:body response)]
         (reset! doc data))))

(defn row [label input]
  [:div.row
    [:div.col-md-2 [:label label]]
    [:div.col-md-5 input]])

(def form-template
  [:div
   (row "name" 
        [:input {:field :text :id :name}])
   (row "phase" 
        [:input {:field :text :id :phase}])
   (row "phase2" 
        [:select {:field :list :id :phase}
         [:option {:key "new"} "Neu"]
         [:option {:key "nominate"} "Anmeldung"]
         [:option {:key "rate"} "Bewertung"]
         [:option {:key "published"} "Ausgewerted"]
         [:option {:key "closed"} "Abgeschlossen"]])])
       
(defn form []
  (fn []
    [:div
     [:div.page-header [:h1 "Edit Contest"]]
     [bind-fields form-template doc]
     [:label (str @doc)]]))
  
(defn render-component [id]
  (fetch-data! id)
  (reagent/render-component [form]
                          (js/document.getElementById "app")))