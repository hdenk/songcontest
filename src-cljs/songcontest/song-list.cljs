(ns songcontest.song-list
  (:require-macros [cljs.core.async.macros :refer (go)])
  (:require
   [reagent.core :as reagent :refer [atom]]
   [cljs-http.client :as http]
   [cljs.core.async :refer (<!)]
   [schema.core :as s]))

(enable-console-print!)

(defonce document (atom #{}))

;; initial call to get songs from server
(go (let [response
          (<! (http/get "/api/song"))
          data (:body response)]
      (reset! document (set data))))

;;; crud operations

(defn remove-by-id [s id]
  (set (remove #(= id (:id %)) s)))

(defn add-song! [song]
     (go (let [response 
               (<! (http/post "/api/song" {:edn-params
                                                 song}))]
          (swap! document conj (:body response)))))

(defn remove-song! [song]
  (go (let [response
            (<! (http/delete (str "/api/song/"
                                  (:id song))))]
        (if (= 200 (:status response))
          (swap! document remove-by-id (:id song))))))
 
(defn update-song! [song]
  (go (let [response
            (<! (http/put (str "/api/song/" (:id song))
                          {:edn-params song}))
            updated-song (:body response)]
        (swap! document
               (fn [old-state]
                 (conj
                  (remove-by-id old-state (:id song))
                  updated-song))))))

;;; end crud operations

(defn editable-input [atom key]
  (if (:editing? @atom)
    [:input {:type     "text"
             :value    (get @atom key)
             :on-change (fn [e] (swap! atom
                                       assoc key
                                       (.. e -target -value)))}]
    [:p (get @atom key)]))

(defn input-valid? [atom]
  (and (seq (-> @atom :artist))
       (seq (-> @atom :title))))

(defn song-row [a]
  (let [row-state (atom {:editing? false
                         :artist     (:artist a)
                         :title  (:title a)})
        current-song (fn []
                       (assoc a
                              :artist (:artist @row-state)
                              :title (:title @row-state)))]
    (fn []
      [:tr
       [:td [editable-input row-state :artist]]
       [:td [editable-input row-state :title]]
       [:td [:button.btn.btn-primary.pull-right
             {:disabled (not (input-valid? row-state))
              :on-click (fn []
                         (when (:editing? @row-state)
                           (update-song! (current-song)))
                         (swap! row-state update-in [:editing?] not))}
             (if (:editing? @row-state) "Save" "Edit")]]
       [:td [:button.btn.pull-right.btn-danger
             {:on-click #(remove-song! (current-song))}
             "\u00D7"]]])))

(defn song-form []
  (let [initial-form-values {:artist     ""
                             :title  ""
                             :editing? true}
        form-input-state (atom initial-form-values)]
    (fn []
      [:tr
       [:td [editable-input form-input-state :artist]]
       [:td [editable-input form-input-state :title]]
       [:td [:button.btn.btn-primary.pull-right
             {:disabled (not (input-valid? form-input-state))
              :on-click  (fn []
                          (add-song! @form-input-state)
                          (reset! form-input-state initial-form-values))}
             "Add"]]])))

(defn song-list []
  [:div
   [:table.table.table-striped
    [:thead
     [:tr
      [:th "Artist"] [:th "Title"] [:th ""] [:th ""]]]
    [:tbody
     (map (fn [song]
            ^{:key (str "song-row-" (:id song))}
            [song-row song])
          (sort-by :artist @document))
     [song-form]]]])

(defn render-component []
  (reagent/render-component [song-list]
                          (js/document.getElementById "app")))
