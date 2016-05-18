(ns songcontest.test-api
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [songcontest.api :as api]
            [songcontest.test-persist :refer [init-db!]]))

(use-fixtures :once init-db!)

;;; Tests

(deftest version
  (let [response (api/handler (mock/request :get "/version"))]
    (is (= (:status response) 200))
    (is (.startsWith (get-in response [:headers "Content-Type"]) "text/html"))
    (is (.startsWith (:body response) "Songcontest Version"))))

(deftest not-found
  (let [response (api/handler (mock/request :get "/bogus-route"))]
    (is (= (:status response) 404))))

;; Contest

(deftest get-contest
  (let [response (api/handler (mock/request :get "/api/contest"))]
    (is (= (:status response) 200))
    (is (.startsWith (get-in response [:headers "Content-Type"]) "application/edn"))))

(deftest post-contest  
  (let [contest {:name "#101" :phase "new"}
        response (api/handler (mock/request :post "/api/contest" contest))]
    (is (= (:status response) 303))
    (is (.startsWith (get-in response [:headers "Content-Type"]) "application/edn"))
    (is (.startsWith (get-in response [:headers "Location"]) "/api/contest/"))))
  
;; Motto

(deftest get-motto
  (let [response (api/handler (mock/request :get "/api/motto"))]
    (is (= (:status response) 200))
    (is (.startsWith (get-in response [:headers "Content-Type"]) "application/edn"))))

(deftest post-motto  
  (let [motto {:name "70er" :comment "Songs der 70er Jahre"}
        response (api/handler (mock/request :post "/api/motto" motto))]
    (is (= (:status response) 303))
    (is (.startsWith (get-in response [:headers "Content-Type"]) "application/edn"))
    (is (.startsWith (get-in response [:headers "Location"]) "/api/motto/"))))
  
;; Song

(deftest get-song
 (let [response (api/handler (mock/request :get "/api/song"))]
   (is (= (:status response) 200))
   (is (.startsWith (get-in response [:headers "Content-Type"]) "application/edn"))))

(deftest post-song 
  (let [song {:artist "Monster Magnet" :title "Look to the Orb for the Warning"}
        response (api/handler (mock/request :post "/api/song" song))]
    (is (= (:status response) 303))
    (is (.startsWith (get-in response [:headers "Content-Type"]) "application/edn"))
    (is (.startsWith (get-in response [:headers "Location"]) "/api/song/"))))

(run-tests)