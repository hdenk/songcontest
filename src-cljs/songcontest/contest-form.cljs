(ns songcontest.contest-form
  (:require-macros [cljs.core.async.macros :refer (go)])
  (:require
   [reagent.core :as reagent :refer [atom]]
   [reagent-forms.core :refer [bind-fields]]
   [cljs-http.client :as http]
   [cljs.core.async :refer (<!)]
   [schema.core :as s]))

(enable-console-print!)

(defn row [label input]
  [:div.row
    [:div.col-md-2 [:label label]]
    [:div.col-md-5 input]])

(def form-template
  [:div
   (row "first name" [:input {:field :text :id :person.first-name}])
   (row "last name" [:input {:field :text :id :person.last-name}])
   (row "age" [:input {:field :numeric :id :person.age}])
   (row "email" [:input {:field :email :id :person.email}])
   (row "comments" [:textarea {:field :textarea :id :comments}])])

(defn form []
  (let [doc (atom {})]
    (fn []
      [:div
       [:div.page-header [:h1 "Reagent Form"]]
       [bind-fields form-template doc]
       [:label (str @doc)]])))

(defn render-component [id]
  (reagent/render-component [form]
                          (js/document.getElementById "app")))

#_
(go (let [response
          (<! (http/get "/api/contest"))
          data (:body response)]
      (reset! document (set data))))
