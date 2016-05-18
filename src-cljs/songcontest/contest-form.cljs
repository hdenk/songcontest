(ns songcontest.contest-form
  (:require-macros [cljs.core.async.macros :refer (go)])
  (:require
   [reagent.core :as reagent :refer [atom]]
   [reagent-forms.core :refer [bind-fields]]
   [cljs-http.client :as http]
   [cljs.core.async :refer (<!)]
   [schema.core :as s]))

(enable-console-print!)

(defonce doc (atom {:contest {:name "" :phase ""}
                    :motto #{}}))

(defn fetch-data! [id]
   (let [response (http/get "/api/motto")
         data (:body response)]
     (swap! doc update-in [:motto] (fn [_] (set data))))
   (go (let [response
             (<! (http/get (str "/api/contest/" id)))
             data (:body response)]
         (swap! doc update-in [:contest] (fn [_] data)))))
   
(defn row [label input]
  [:div.row
    [:div.col-md-2 [:label label]]
    [:div.col-md-5 input]])

(def dummy-mottos 
     #{
        {:modified_at nil, :name "M1", :active "T", :id 1, :comment "C1", :created_at #inst "2016-05-18T14:27:56.355-00:00"} 
        {:modified_at nil, :name "M2", :active "T", :id 2, :comment "C2", :created_at #inst "2016-05-18T14:28:02.167-00:00"}})

(def form-template
  [:div
   (row "Name" 
        [:input {:field :text :id :contest.name}])
   (row "Phase" 
        [:input {:field :text :id :contest.phase}])
   (row "Motto" 
        [:select {:field :list :id :contest.motto}
          (map (fn [motto] (js/console.log "###Motto###") [:option {:key (str (:id motto))} (:name motto)])
               (:motto @doc))]) 
   (row "Phase2"
         [:input {:field :text :id :contest.phase}])
   [:h3 "Phasen"]
   [:div.btn-group {:field :single-select :id :contest.phase}
    [:button.btn.btn-default {:key "new"} "Neu"]
    [:button.btn.btn-default {:key "nominate"} "Anmeldung"]
    [:button.btn.btn-default {:key "rate"} "Bewertung"]
    [:button.btn.btn-default {:key "rated"} "Ausgewertet"]
    [:button.btn.btn-default {:key "closed"} "Abgeschlossen"]]])

(defn form []
  (fn []
    [:div
     [:div.page-header [:h1 "Edit Contest"]]
     [bind-fields form-template doc]
     [:label (str @doc)]]))
  
(defn render-component [id]
  (js/console.log "###render-component*###") ; TODO entfernen !!!
  (fetch-data! id)
  (reagent/render-component [form]
                          (js/document.getElementById "app")))