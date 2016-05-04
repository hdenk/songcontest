(ns songcontest.nomination
  (:require-macros [cljs.core.async.macros :refer (go)])
  (:require
   [reagent.core :as reagent :refer [atom]]
   [cljs-http.client :as http]
   [cljs.core.async :refer (<!)]
   [schema.core :as s]))

(enable-console-print!)

(defonce app-state (atom #{}))

;; initial call to get nominations from server
(go (let [response
          (<! (http/get "/api/nomination"))
          data (:body response)]
      (reset! app-state (set data))))

;;; crud operations

(defn remove-by-id [s id]
  (set (remove #(= id (:id %)) s)))

(defn add-nomination! [nomination]
     (go (let [response 
               (<! (http/post "/api/nomination" {:edn-params
                                                 nomination}))]
          (swap! app-state conj (:body response)))))

(defn remove-nomination! [nomination]
  (go (let [response
            (<! (http/delete (str "/api/nomination/"
                                  (:id nomination))))]
        (if (= 200 (:status response))
          (swap! app-state remove-by-id (:id nomination))))))
 
(defn update-nomination! [nomination]
  (go (let [response
            (<! (http/put (str "/api/nomination/" (:id nomination))
                          {:edn-params nomination}))
            updated-nomination (:body response)]
        (swap! app-state
               (fn [old-state]
                 (conj
                  (remove-by-id old-state (:id nomination))
                  updated-nomination))))))

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
       (seq (-> @atom :species))))

(defn nomination-row [a]
  (let [row-state (atom {:editing? false
                         :name     (:name a)
                         :species  (:species a)})
        current-nomination (fn []
                            (assoc a
                                   :name (:name @row-state)
                                   :species (:species @row-state)))]
    (fn []
      [:tr
       [:td [editable-input row-state :name]]
       [:td [editable-input row-state :species]]
       [:td [:button.btn.btn-primary.pull-right
             {:disabled (not (input-valid? row-state))
              :on-click (fn []
                         (when (:editing? @row-state)
                           (update-nomination! (current-nomination)))
                         (swap! row-state update-in [:editing?] not))}
             (if (:editing? @row-state) "Save" "Edit")]]
       [:td [:button.btn.pull-right.btn-danger
             {:on-click #(remove-nomination! (current-nomination))}
             "\u00D7"]]])))

(defn nomination-form []
  (let [initial-form-values {:name     ""
                             :species  ""
                             :editing? true}
        form-input-state (atom initial-form-values)]
    (fn []
      [:tr
       [:td [editable-input form-input-state :name]]
       [:td [editable-input form-input-state :species]]
       [:td [:button.btn.btn-primary.pull-right
             {:disabled (not (input-valid? form-input-state))
              :on-click  (fn []
                          (add-nomination! @form-input-state)
                          (reset! form-input-state initial-form-values))}
             "Add"]]])))

(defn nominations []
  [:div
   [:table.table.table-striped
    [:thead
     [:tr
      [:th "Name"] [:th "Species"] [:th ""] [:th ""]]]
    [:tbody
     (map (fn [nomination]
            ^{:key (str "nomination-row-" (:id nomination))}
            [nomination-row nomination])
          (sort-by :name @app-state))
     [nomination-form]]]])

(defn render-component []
  (reagent/render-component [nominations]
                          (js/document.getElementById "app")))
