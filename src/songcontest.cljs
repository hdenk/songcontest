(ns songcontest
 (:require [reagent.session :as session]
           [reagent.core :as reagent :refer [atom]]
           [secretary.core :as secretary
             :include-macros true :refer [defroute]]
           [songbook.pages.user :as user-pages]
           [goog.events :as events]
           [goog.history.EventType :as EventType]))

(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
      EventType/NAVIGATE
      (fn [event]
        (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

(defroute "/" []
  (session/put! :current-page user-pages/list-page))
(defroute "/sign-in" []
  (session/put! :current-page user-pages/edit-page))

(def current-page (atom nil))

(defn page []
  [(session/get :current-page)])

(defn ^:export init! []
  (secretary/set-config! :prefix "#")
  (session/put! :current-page user-pages/list-page)
  #_(hook-browser-navigation!)
  (reagent/render-component [page] (.-body js/document)))
