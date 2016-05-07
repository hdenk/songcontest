(ns songcontest.test-persist
  (:require [clojure.test :refer :all]
            [clojure.java.jdbc :as jdbc]
            [songcontest.db :as db]
            [songcontest.persist :as persist]))

(defmulti create-table-ddl identity)
  
(defmethod create-table-ddl :contest [table]  
  (jdbc/create-table-ddl table [:id "BIGINT PRIMARY KEY AUTO_INCREMENT"]
                               [:name "VARCHAR NOT NULL"]
                               [:phase "VARCHAR NOT NULL"]))

(defmethod create-table-ddl :nomination [table]  
  (jdbc/create-table-ddl table [:id "BIGINT PRIMARY KEY AUTO_INCREMENT"]
                               [:name "VARCHAR NOT NULL"]
                               [:phase "VARCHAR NOT NULL"]))

(defn create-test-table! [table]
    (jdbc/db-do-commands db/db (create-table-ddl table)))

(defn clean-up!
  "Attempt to drop any test tables before we start a test."
  [test-function]
  (jdbc/with-db-transaction [t-conn db/db]
    (doseq [table [:contest :nomination]]
      (try
        (jdbc/db-do-commands t-conn (jdbc/drop-table-ddl table))
        (catch Exception _)))) ;; ignore !?
  (test-function))

;;; Fixtures

(use-fixtures :each clean-up!)

(def contest1 {:name "#101" :phase :closed})
(def contest2 {:name "#102" :phase :new})

;;; Tests

(deftest contest-crud []
  (create-test-table! :contest)
  (testing "read on empty table"
    (is (= (count (persist/read-contest db/db)) 0)))
  (testing "create"
    (persist/create-contest! db/db contest1)
    (let [read-result (persist/read-contest db/db)]
      (is (and (= (count read-result) 1)
               (= (:name (first read-result)) (:name contest1))
               (= (:phase (first read-result)) (:phase contest1))))))
  (testing "update"
    (let [read-result (persist/read-contest db/db)
          id (:id read-result)]
      (persist/update-contest! db/db id contest2)
      (let [read-result2 (persist/read-contest db/db)]
        (is (and (= (count read-result2) 1)
               (= (:name (first read-result2)) (:name contest2))
               (= (:phase (first read-result2)) (:phase contest2))))))))
        
(run-tests)
