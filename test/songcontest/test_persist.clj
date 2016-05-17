(ns songcontest.test-persist
  (:require [clojure.test :refer :all]
            [clojure.java.jdbc :as jdbc]
            [songcontest.db :as db]
            [songcontest.persist :as persist]))

(defmulti create-table-ddl identity)
  
(defmethod create-table-ddl :contest [table]  
  (jdbc/create-table-ddl table [[:id "BIGINT PRIMARY KEY AUTO_INCREMENT"]
                                [:name "VARCHAR NOT NULL"]
                                [:phase "VARCHAR NOT NULL"]
                                [:motto "BIGINT"]
                                [:created_at "TIMESTAMP"]
                                [:modified_at "TIMESTAMP"]]))

(defmethod create-table-ddl :song [table]  
  (jdbc/create-table-ddl table [[:id "BIGINT PRIMARY KEY AUTO_INCREMENT"]
                                [:artist "VARCHAR NOT NULL"]
                                [:title "VARCHAR NOT NULL"]
                                [:created_at "TIMESTAMP"]
                                [:modified_at "TIMESTAMP"]]))
                                
(defmethod create-table-ddl :nomination [table]  
  (jdbc/create-table-ddl table [[:id "BIGINT PRIMARY KEY AUTO_INCREMENT"]
                                [:maedchen "BIGINT NOT NULL"]
                                [:contest "BIGINT NOT NULL"]
                                [:song "BIGINT NOT NULL"]
                                [:created_at "TIMESTAMP"]
                                [:modified_at "TIMESTAMP"]]))
                                                                
(defn create-test-table! [table]
    (jdbc/db-do-commands db/db (create-table-ddl table)))

(def table-names [:contest :song :nomination])

(defn clean-up!
  "Attempt to drop any test tables before we start a test."
  [test-function]
  (jdbc/with-db-transaction [t-conn db/db]
    (doseq [table table-names]
      (try
        (jdbc/db-do-commands t-conn (jdbc/drop-table-ddl table))
        (catch Exception _)))) ;; ignore !? (table doesnt exist)
  (test-function))

;;; Fixtures

(use-fixtures :each clean-up!)

(def contest1 {:name "#101" :phase "closed"})
(def contest2 {:name "#102" :phase "new"})
(def song1 {:artist "Sonic Youth" :title "Shadow of a Doubt"})
(def song2 {:artist "Monster Magnet" :title "Look to the Orb for the Warning"})
(def nomination1 {:maedchen 0 :contest 0 :song 0})
(def nomination2 {:maedchen 1 :contest 1 :song 1})

;;; Tests

;; Contest

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

;; Song

(deftest create-song []
  (create-test-table! :song)
  (is (= (count (persist/read-song db/db)) 0))
  (persist/create-song! db/db song1)
  (let [created (persist/read-song db/db)]
    (is (= (count created) 1))
    (is (= (:name (first created)) (:name song1)))
    (is (= (:phase (first created)) (:phase song1)))))

(deftest update-song []
  (create-test-table! :song)
  (persist/create-song! db/db song1)
  (let [created (persist/read-song db/db)
        id-created (:id (first created))]
    (persist/update-song! db/db id-created song2)
    (let [updated (persist/read-song db/db)
          {:keys [:id :name :phase]} (first updated)]
      (is (= (count updated) 1))
      (is (= id id-created))
      (is (= name (:name song2)))
      (is (= phase (:phase song2))))))

(deftest delete-song []
  (create-test-table! :song)
  (persist/create-song! db/db song1)
  (let [created (persist/read-song db/db)
        id (:id (first created))]
    (persist/delete-song! db/db id)
    (is (= (count (persist/read-song db/db)) 0))))

;; Nomination

(deftest create-nomination []
  (create-test-table! :nomination)
  (is (= (count (persist/read-nomination db/db)) 0))
  (persist/create-nomination! db/db nomination1)
  (let [created (persist/read-nomination db/db)]
    (is (= (count created) 1))
    (is (= (:maedchen (first created)) (:maedchen nomination1)))
    (is (= (:contest (first created)) (:contest nomination1)))
    (is (= (:song (first created)) (:song nomination1)))))

(deftest update-nomination []
  (create-test-table! :nomination)
  (persist/create-nomination! db/db nomination1)
  (let [created (persist/read-nomination db/db)
        id-created (:id (first created))]
    (persist/update-nomination! db/db id-created nomination2)
    (let [updated (persist/read-nomination db/db)
          {:keys [:id :maedchen :contest :song]} (first updated)]
      (is (= (count updated) 1))
      (is (= id id-created))
      (is (= maedchen (:maedchen nomination2)))
      (is (= contest (:contest nomination2)))
      (is (= song (:song nomination2))))))

(deftest delete-nomination []
  (create-test-table! :nomination)
  (persist/create-nomination! db/db nomination1)
  (let [created (persist/read-nomination db/db)
        id (:id (first created))]
    (persist/delete-nomination! db/db id)
    (is (= (count (persist/read-nomination db/db)) 0))))

;(run-tests)

(defn init-db! [test-function]
  (clean-up! (fn []))
  (doseq [table table-names]
    (create-test-table! table))
  (test-function))

(init-db! (fn []))