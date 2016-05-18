(ns songcontest.contest-list
  (:require-macros [cljs.core.async.macros :refer (go)])
  (:require
   [reagent.core :as reagent :refer [atom]]
   [cljs-http.client :as http]
   [cljs.core.async :refer (<!)]
   [schema.core :as s]))

(enable-console-print!)

(defonce doc (atom #{}))

;; initial call to get contests from server
(go (let [response
          (<! (http/get "/api/contest"))
          data (:body response)]
      (reset! doc (set data))))

;;; crud operations

(defn remove-by-id [s id]
  (set (remove #(= id (:id %)) s)))

(defn add-contest! [contest]
     (go (let [response 
               (<! (http/post "/api/contest" {:edn-params
                                              contest}))]
          (swap! doc conj (:body response)))))

(defn remove-contest! [contest]
  (go (let [response
            (<! (http/delete (str "/api/contest/"
                                  (:id contest))))]
        (if (= 200 (:status response))
          (swap! doc remove-by-id (:id contest))))))

(defn edit-contest [])

(defn update-contest! [contest]
  (go (let [response
            (<! (http/put (str "/api/contest/" (:id contest))
                          {:edn-params contest}))
            updated-contest (:body response)]
        (swap! doc
               (fn [old-state]
                 (conj
                  (remove-by-id old-state (:id contest))
                  updated-contest))))))

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
  (and (seq (-> @atom :name))
       (seq (-> @atom :phase))))

(defn contest-row [a]
  (let [row-state (atom {:editing? false
                         :name (:name a)
                         :phase (:phase a)})
        current-contest (fn []
                          (assoc a
                                 :name (:name @row-state)
                                 :phase (:phase @row-state)))]
    (fn []
      [:tr
       [:td [editable-input row-state :name]]
       [:td [editable-input row-state :phase]]
       [:td [:button.btn.btn-primary.pull-right
             {:disabled (not (input-valid? row-state))
              :on-click (fn []
                         (when (:editing? @row-state)
                           (update-contest! (current-contest)))
                         (swap! row-state update-in [:editing?] not))}
             (if (:editing? @row-state) "Save*" "Edit")]
          [:a {:href (str "contest/" (:id a))} "Edit2"]] ; TODO entfernen !!!!!
        
       [:td [:button.btn.pull-right.btn-danger
             {:on-click #(remove-contest! (current-contest))}
             "\u00D7"]]])))

(defn contest-form []
  (let [initial-form-values {:name ""
                             :phase ""
                             :editing? true}
        form-input-state (atom initial-form-values)]
    (fn []
      [:tr
       [:td [editable-input form-input-state :name]]
       [:td [editable-input form-input-state :phase]]
       [:td [:button.btn.btn-primary.pull-right
             {:disabled (not (input-valid? form-input-state))
              :on-click  (fn []
                          (add-contest! @form-input-state)
                          (reset! form-input-state initial-form-values))}
             "Add"]]])))

(defn contest-list []
  [:div
   [:table.table.table-striped
    [:thead
     [:tr
      [:th "Name"] [:th "Phase"] [:th ""] [:th ""]]]
    [:tbody
     (map (fn [contest]
            ^{:key (str "contest-row-" (:id contest))}
            [contest-row contest])
          (sort-by :name @doc))
     [contest-form]
     [:label (str @doc)]]]])

(defn render-component []
  (reagent/render-component [contest-list]
                          (js/document.getElementById "app")))
