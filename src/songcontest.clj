(ns songcontest
  (:require [compojure.core :refer [GET POST defroutes routes context]]
            [compojure.route :as route]
            [compojure.handler :as handler]
            [ring.middleware.edn :refer [wrap-edn-params]]))

(defn generate-response [data & [status]]
  {:status (or status 200)
   :headers {"Content-Type" "application/edn"}
   :body (pr-str data)})

(defroutes rest-api-routes
  (context "/user/:id" [id]
           (GET "/rest" req
                (generate-response {:message "Hello stranger"}))
           (POST "/rest" {edn-params :edn-params}
                 (generate-response
                  {:message (str "Hello " (->> edn-params :name reverse (apply str)))}))))

(defroutes site-routes
  (GET "/" req (slurp "index.html"))
  #_(route/resources "/"))

(def server-routes
  (-> (routes (handler/api rest-api-routes)
              (handler/site site-routes))
      wrap-edn-params))
