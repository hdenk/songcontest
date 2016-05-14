(ns songcontest.test-api
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [songcontest.api :as api]
            [songcontest.test-persist :refer [init-db!]]))

(use-fixtures :once init-db!)

(deftest test-api
  (testing "version"
    (let [response (api/handler (mock/request :get "/version"))]
      (is (= (:status response) 200))
      (is (= (get-in response [:headers "Content-Type"]) "text/html; charset=utf-8"))
      (is (= (:body response) "Songcontest 0.1.0"))))
  
  (testing "contest"
    (let [response (api/handler (mock/request :get "/api/contest"))]
      (is (= (:status response) 200))
      (is (= (get-in response [:headers "Content-Type"]) "application/edn;charset=UTF-8"))))

  (testing "song"
   (let [response (api/handler (mock/request :get "/api/song"))]
     (is (= (:status response) 200))
     (is (= (get-in response [:headers "Content-Type"]) "application/edn;charset=UTF-8"))))

  (testing "not-found route"
    (let [response (api/handler (mock/request :get "/bogus-route"))]
      (is (= (:status response) 404)))))

(run-tests)