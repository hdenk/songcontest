(ns songcontest.promotion
  (:require-macros [cljs.core.async.macros :refer (go)])
  (:require
   [reagent.core :as reagent :refer [atom]]
   [cljs-http.client :as http]
   [cljs.core.async :refer (<!)]
   [schema.core :as s]))

(enable-console-print!)

(defonce app-state (atom #{}))

;; initial call to get promotions from server
(go (let [response
          (<! (http/get "/api/promotion"))
          data (:body response)]
      (reset! app-state (set data))))

;;; crud operations

(defn remove-by-id [s id]
  (set (remove #(= id (:id %)) s)))

(defn add-promotion! [promotion]
     (go (let [response 
               (<! (http/post "/api/promotion" {:edn-params
                                                promotion}))]
          (swap! app-state conj (:body response)))))

(defn remove-promotion! [promotion]
  (go (let [response
            (<! (http/delete (str "/api/promotion/"
                                  (:id promotion))))]
        (if (= 200 (:status response))
          (swap! app-state remove-by-id (:id promotion))))))
 
(defn update-promotion! [promotion]
  (go (let [response
            (<! (http/put (str "/api/promotion/" (:id promotion))
                          {:edn-params promotion}))
            updated-promotion (:body response)]
        (swap! app-state
               (fn [old-state]
                 (conj
                  (remove-by-id old-state (:id promotion))
                  updated-promotion))))))

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

(defn promotion-row [a]
  (let [row-state (atom {:editing? false
                         :name     (:name a)
                         :species  (:species a)})
        current-promotion (fn []
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
                           (update-promotion! (current-promotion)))
                         (swap! row-state update-in [:editing?] not))}
             (if (:editing? @row-state) "Save" "Edit")]]
       [:td [:button.btn.pull-right.btn-danger
             {:on-click #(remove-promotion! (current-promotion))}
             "\u00D7"]]])))

(defn promotion-form []
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
                          (add-promotion! @form-input-state)
                          (reset! form-input-state initial-form-values))}
             "Add"]]])))

(defn promotions []
  [:div
   [:table.table.table-striped
    [:thead
     [:tr
      [:th "Name"] [:th "Species"] [:th ""] [:th ""]]]
    [:tbody
     (map (fn [promotion]
            ^{:key (str "promotion-row-" (:id promotion))}
            [promotion-row promotion])
          (sort-by :name @app-state))
     [promotion-form]]]])

(defn render-component []
  (reagent/render-component [promotions]
                          (js/document.getElementById "app")))
