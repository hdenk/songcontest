(ns songcontest.motto-list
  (:require-macros [cljs.core.async.macros :refer (go)])
  (:require
   [reagent.core :as reagent :refer [atom]]
   [cljs-http.client :as http]
   [cljs.core.async :refer (<!)]
   [schema.core :as s]))

(enable-console-print!)

(defonce doc (atom #{}))

;; initial call to get mottos from server
(go (let [response
          (<! (http/get "/api/motto"))
          data (:body response)]
      (reset! doc (set data))))

;;; crud operations

(defn remove-by-id [s id]
  (set (remove #(= id (:id %)) s)))

(defn add-motto! [motto]
     (go (let [response 
               (<! (http/post "/api/motto" {:edn-params
                                                 motto}))]
          (swap! doc conj (:body response)))))

(defn remove-motto! [motto]
  (go (let [response
            (<! (http/delete (str "/api/motto/"
                                  (:id motto))))]
        (if (= 200 (:status response))
          (swap! doc remove-by-id (:id motto))))))
 
(defn update-motto! [motto]
  (go (let [response
            (<! (http/put (str "/api/motto/" (:id motto))
                          {:edn-params motto}))
            updated-motto (:body response)]
        (swap! doc
               (fn [old-state]
                 (conj
                  (remove-by-id old-state (:id motto))
                  updated-motto))))))

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
       (seq (-> @atom :comment))))

(defn motto-row [a]
  (let [row-state (atom {:editing? false
                         :name     (:name a)
                         :comment  (:comment a)})
        current-motto (fn []
                       (assoc a
                              :name (:name @row-state)
                              :comment (:comment @row-state)))]
    (fn []
      [:tr
       [:td [editable-input row-state :name]]
       [:td [editable-input row-state :comment]]
       [:td [:button.btn.btn-primary.pull-right
             {:disabled (not (input-valid? row-state))
              :on-click (fn []
                         (when (:editing? @row-state)
                           (update-motto! (current-motto)))
                         (swap! row-state update-in [:editing?] not))}
             (if (:editing? @row-state) "Save" "Edit")]]
       [:td [:button.btn.pull-right.btn-danger
             {:on-click #(remove-motto! (current-motto))}
             "\u00D7"]]])))

(defn motto-form []
  (let [initial-form-values {:name     ""
                             :comment  ""
                             :editing? true}
        form-input-state (atom initial-form-values)]
    (fn []
      [:tr
       [:td [editable-input form-input-state :name]]
       [:td [editable-input form-input-state :comment]]
       [:td [:button.btn.btn-primary.pull-right
             {:disabled (not (input-valid? form-input-state))
              :on-click  (fn []
                          (add-motto! @form-input-state)
                          (reset! form-input-state initial-form-values))}
             "Add"]]])))

(defn motto-list []
  [:div
   [:table.table.table-striped
    [:thead
     [:tr
      [:th "name"] [:th "comment"] [:th ""] [:th ""]]]
    [:tbody
     (map (fn [motto]
            ^{:key (str "motto-row-" (:id motto))}
            [motto-row motto])
          (sort-by :name @doc))
     [motto-form]
     [:label (str @doc)]]]])

(defn render-component []
  (reagent/render-component [motto-list]
                          (js/document.getElementById "app")))
