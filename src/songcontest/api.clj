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
     [songcontest.page :as page]
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
       [name phase]
       (resource
        :available-media-types ["application/edn"]
        :allowed-methods [:get :post]
        :handle-ok (fn [ctx]
                     (let [found (persist/read-contest db/db)]
                       (condp = (-> ctx :representation :media-type)
                         "application/edn" found
                         "application/json" (json/generate-string found))))
        :post! (fn [ctx] 
                 (let [c (schema/coerce-params->contest {:name name 
                                                         :phase phase})]
                   {::id (persist/create-contest! db/db c)})) 
        :post-redirect? (fn [ctx] {:location (str "/api/contest/" (::id ctx))})
        :handle-exception handle-exception))

  (ANY "/api/contest/:id"
       [id name phase]
       (let [id (edn/read-string id)]
         (resource
           :available-media-types ["application/edn"]
           :allowed-methods [:get :put :delete]
           :handle-ok (fn [ctx]
                        (persist/read-contest db/db id))
           :put! (fn [ctx]
                   (let [c (schema/coerce-params->contest {:name name 
                                                           :phase phase})]
                     (persist/update-contest! db/db id c)))
           :new? false
           :respond-with-entity? true
           :delete! (fn [ctx] (persist/delete-contest! db/db id))
           :handle-exception handle-exception)))

  (ANY "/api/motto"
       [name comment]
       (resource
        :available-media-types ["application/edn"]
        :allowed-methods [:get :post]
        :handle-ok (fn [ctx]
                     (let [found (persist/read-motto db/db)]
                       (condp = (-> ctx :representation :media-type)
                         "application/edn" found
                         "application/json" (json/generate-string found))))
        :post! (fn [ctx] 
                 (let [c (schema/coerce-params->motto {:name name 
                                                       :comment comment})]
                   {::id (persist/create-motto! db/db c)})) 
        :post-redirect? (fn [ctx] {:location (str "/api/motto/" (::id ctx))})
        :handle-exception handle-exception))

  (ANY "/api/motto/:id"
       [id name comment]
       (let [id (edn/read-string id)]
         (resource
           :available-media-types ["application/edn"]
           :allowed-methods [:get :put :delete]
           :handle-ok (fn [ctx]
                        (persist/read-motto db/db id))
           :put! (fn [ctx]
                   (let [c (schema/coerce-params->motto {:name name 
                                                         :comment comment})]
                     (persist/update-motto! db/db id c)))
           :new? false
           :respond-with-entity? true
           :delete! (fn [ctx] (persist/delete-motto! db/db id))
           :handle-exception handle-exception)))

  (ANY "/api/song"
       [artist title]
       (resource
        :available-media-types ["application/edn"]
        :allowed-methods [:get :post]
        :handle-ok (fn [ctx]
                     (let [found (persist/read-song db/db)]
                       (condp = (-> ctx :representation :media-type)
                         "application/edn" found
                         "application/json" (json/generate-string found))))
        :post! (fn [ctx] 
                 (let [c (schema/coerce-params->song {:artist artist 
                                                      :title title})]
                   {::id (persist/create-song! db/db c)})) 
        :post-redirect? (fn [ctx] {:location (str "/api/song/" (::id ctx))})
        :handle-exception handle-exception))

  (ANY "/api/song/:id"
       [id artist title]
       (let [id (edn/read-string id)]
         (resource
           :available-media-types ["application/edn"]
           :allowed-methods [:get :put :delete]
           :handle-ok (fn [ctx]
                        (persist/read-song db/db id))
           :put! (fn [ctx]
                   (let [c (schema/coerce-params->song {:artist artist 
                                                        :title title})]
                     (persist/update-song! db/db id c)))
           :new? false
           :respond-with-entity? true
           :delete! (fn [ctx] (persist/delete-song! db/db id))
           :handle-exception handle-exception)))
  
  (GET "/version" 
       []
       "Songcontest Version 0.1.0") ; TODO take Version from project.clj
  
  (GET "/" 
       []
      (page/index))

  (GET "/contest" 
       []
       (page/default "contest_list"))

  (GET "/contest/:id" 
       [id]
       (page/default "contest_form" id))

  (GET "/motto" 
     []
     (page/default "motto_list"))


  (GET "/song" 
       []
       (page/default "song_list"))

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