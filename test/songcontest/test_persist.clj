(ns songcontest.test-persist
  (:require [clojure.test :refer :all]
            [clojure.java.jdbc :as jdbc]
            [songcontest.db :as db]
            [songcontest.persist :as persist]))

(defmulti create-table-ddl identity)
  
(defmethod create-table-ddl :contest [table]  
  (jdbc/create-table-ddl table [[:id "BIGINT PRIMARY KEY AUTO_INCREMENT"]
                                [:name "VARCHAR NOT NULL"]
                                [:phase "VARCHAR NOT NULL"]]))

(defmethod create-table-ddl :nomination [table]  
  (jdbc/create-table-ddl table [[:id "BIGINT PRIMARY KEY AUTO_INCREMENT"]
                                [:name "VARCHAR NOT NULL"]
                                [:phase "VARCHAR NOT NULL"]]))

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
  (let [created (persist/read-contest db/db)]
    (is (= (count created) 1))
    (is (= (:name (first created)) (:name contest1)))
    (is (= (:phase (first created)) (:phase contest1)))))

(deftest update-contest []
  (create-test-table! :contest)
  (persist/create-contest! db/db contest1)
  (let [created (persist/read-contest db/db)
        id-created (:id (first created))]
    (persist/update-contest! db/db id-created contest2)
    (let [updated (persist/read-contest db/db)
          {:keys [:id :name :phase]} (first updated)]
      (is (= (count updated) 1))
      (is (= id id-created))
      (is (= name (:name contest2)))
      (is (= phase (:phase contest2))))))

(deftest delete-contest []
  (create-test-table! :contest)
  (persist/create-contest! db/db contest1)
  (let [created (persist/read-contest db/db)
        id (:id (first created))]
    (persist/delete-contest! db/db id)
    (is (= (count (persist/read-contest db/db)) 0))))

(run-tests)
