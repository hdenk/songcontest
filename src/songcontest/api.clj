(ns songcontest.api
    (:require
     [liberator.core :refer (resource)]
     [compojure.core :refer (defroutes ANY GET)]
     [compojure.route :refer (resources not-found)]
     [ring.middleware.params :refer (wrap-params)]
     [ring.middleware.edn :refer (wrap-edn-params)]
     [ring.util.response :refer (redirect)]
     [clj-json.core :as json]
     [clojure.edn :as edn]
     [songcontest.pages :as pages]
     [songcontest.schema :as schema]
     [songcontest.db :as db]
     [songcontest.persist :as persist]))

(defn handle-exception
  [ctx]
  (let [e (:exception ctx)]
   (.printStackTrace e)
   {:status 500 :message (.getMessage e)}))

(defroutes routes

  (ANY "/api/contest"
       [name species]
       (resource
        :available-media-types ["application/edn"]
        :allowed-methods [:get :post]
        :handle-ok (fn [ctx]
                     (let [found (persist/read-contest db/db)]
                       (condp = (-> ctx :representation :media-type)
                         "application/edn" found
                         "application/json" (json/generate-string found))))
        :post! (fn [ctx] 
                 (let [c (schema/coerce-and-validate Contest 
                                                     {:name name 
                                                      :phase phase})]
                   {::id (persist/create-contest! db/db c)})) 
                                 
        :post-redirect? (fn [ctx] {:location (str "/api/contest/" (::id ctx))})
        :handle-exception handle-exception))

  (ANY "/api/contest/:id"
       [id name species]
       (let [id (edn/read-string id)]
         (resource
           :available-media-types ["application/edn"]
           :allowed-methods [:get :put :delete]
           :handle-ok (fn [ctx]
                        (persist/read-contest db/db id))
           :put! (fn [ctx]
                   (persist/update-contest!
                     db/db id
                     {:name name :species species}))
           :new? false
           :respond-with-entity? true
           :delete! (fn [ctx] (persist/delete-contest! db/db id))
           :handle-exception handle-exception)))

  (ANY "/api/nomination"
       [name species]
       (resource
        :available-media-types ["application/edn"]
        :allowed-methods [:get :post]
        :handle-ok (fn [ctx]
                     (let [found (persist/read-nomination db/db)]
                       (condp = (-> ctx :representation :media-type)
                         "application/edn" found
                         "application/json" (json/generate-string found))))
        :post! (fn [ctx] {::id (persist/create-nomination! db/db {:name name :species species})})
        :post-redirect? (fn [ctx] {:location (str "/api/nomination/" (::id ctx))})
        :handle-exception handle-exception))

  (ANY "/api/nomination/:id"
       [id name species]
       (let [id (edn/read-string id)]
         (resource
           :available-media-types ["application/edn"]
           :allowed-methods [:get :put :delete]
           :handle-ok (fn [ctx]
                        (persist/read-nomination db/db id))
           :put! (fn [ctx]
                   (persist/update-nomination!
                     db/db id
                     {:name name :species species}))
           :new? false
           :respond-with-entity? true
           :delete! (fn [ctx] (persist/delete-nomination! db/db id))
           :handle-exception handle-exception)))
  
  (GET "/greeting" 
       []
       "Hello World!")
  
  (GET "/" 
       []
      (pages/index))

  (GET "/contest" 
       []
       (pages/user :contest))

  (GET "/nomination" 
       []
       (pages/user :nomination))

  (GET "/rating" 
       []
       (pages/user :rating))

  
  (resources "/" {:root "public"})
  (resources "/" {:root "/META-INF/resources"})
  (not-found "404"))

(def handler
  (-> routes
      wrap-params
      wrap-edn-params))

(defn init
  []
  (println "initializing application")
  ;; check db
  (if-not (db/exists? db/db "contest")
    (println "database check failed")))