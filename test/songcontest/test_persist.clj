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

(deftest create-contest []
  (create-test-table! :contest)
  (is (= (count (persist/read-contest db/db)) 0))
  (persist/create-contest! db/db contest1)
  (let [read-result (persist/read-contest db/db)]
    (is (= (count read-result) 1))
    (is (= (:name (first read-result)) (:name contest1)))
    (is (= (:phase (first read-result)) (:phase contest1)))))

(deftest update-contest []
  (create-test-table! :contest)
  (persist/create-contest! db/db contest1)
  (let [read-result-create (persist/read-contest db/db)
        id-create (:id (first read-result-create))]
    (persist/update-contest! db/db id-create contest2)
    (let [read-result-update (persist/read-contest db/db)
          {:keys [:id :name :phase]} (first read-result-update)]
      (is (and (= (count read-result-update) 1)
               (= id id-create)
               (= name (:name contest2))
               (= phase (:phase contest2)))))))

(deftest delete-contest []
  (create-test-table! :contest)
  (persist/create-contest! db/db contest1)
  (let [read-result (persist/read-contest db/db)
        id (:id (first read-result))]
    (persist/delete-contest! db/db id)
    (is (= (count (persist/read-contest db/db)) 0))))

(run-tests)
