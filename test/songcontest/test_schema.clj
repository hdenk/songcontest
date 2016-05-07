(ns songcontest.test-schema
  (:require [clojure.test :refer :all]
            [songcontest.schema :as schema]))

(def contest1 {:name "#101" :phase "closed"})
(def contest2 {:name "#102" :phase "new"})

(deftest contest []
  (is (= (schema/coerce-contest contest1) {:name "#101" :phase :closed})))

(run-tests)