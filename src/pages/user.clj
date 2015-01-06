(ns songcontest.pages.user
  (:refer-clojure :exclude [get])
  (:require [reagent.core :as reagent :refer [atom]]
            [reagent-forms :as forms]
            [reagent-utils.session :as session]
            [secretary.core :refer [dispatch!]]
            [ajax.core :refer [POST]]))

(defn put! [doc id value]
  (swap! doc assoc :saved? false id value))

(defn get [doc id]
  (id @doc))

(defn row [label input]
  [:div.row
   [:div.col-md-2 [:label label]]
   [:div.col-md-5 input]])

#_(defn text-input [doc id label]
  [row label
   [:input {:type "text"
            :class "form-control"
            :value (get doc id)
            :onChange #(put! doc id (-> % .-target .-value))}]])

(defn save-doc [doc]
  (POST (str js/context "/save")
        {:params (dissoc @doc :saved?)
         :handler
         (fn [_]
           (put! doc :saved? true)
           (session/update-in! [:user] conj @doc)
           (dispatch! "/"))}))

(def edit-form
  [:div
   (input "first name" :text :first-name)
   [:div.row
    [:div.col-md-2]
    [:div.col-md-5
     [:div.alert.alert-danger
      {:field :alert :id :errors.first-name}]]]
   (input "last name" :text :last-name)
   [:div.row
    [:div.col-md-2]
    [:div.col-md-5
     [:div.alert.alert-success
      {:field :alert :id :last-name :event empty?}
      "last name is empty!"]]]
   [:div.row
    [:div.col-md-2 [:label "Age"]]
    [:div.col-md-5
     [:div
      {:field :datepicker :id :age :date-format "yyyy/mm/dd" :inline true}]]]
   (input "email" :email :email)
   (row
    "comments"
    [:textarea.form-control
     {:field :textarea :id :comments}])])

(defn edit-page []
  (let [doc (atom {})]
    (fn []
      [:div
       [:div.page-header [:h1 "Edit"]]
       [bind-fields
        edit-form
        doc
        (fn [[id] value doc]
          (if (get doc :saved?)
            [:p "Saved"]
            [:button {:type "submit"
                      :class "btn btn-default"
                      :on-click #(save-doc doc)}
             "Submit"])
          [:button {:type "submit"
                    :class "btn btn-default"
                    :on-click #(dispatch! "/")} "back"])]])))

(defn list-page []
  [:div
   [:div.page-header [:h2 "Users"]]
   (for [{:keys [first-name last-name]}
         (session/get :user)]
     [:div.row
      [:p first-name " " last-name]])
   [:button {:type "submit"
             :class "btn btn-default"
             :on-click #(dispatch! "/sign-in")}
    "sign in"]])
