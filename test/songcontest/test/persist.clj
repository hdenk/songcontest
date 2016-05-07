(ns songcontest.test.persist
  (:require
    [clojure.test :refer :all]
    [clojure.java.jdbc :as jdbc]
    [songcontest.db :as db]
    [songcontest.persist :as persist]))

;;; Fixtures

(defn drop-tables! [conn]
  (do
    (println "dropping tables")
    (jdbc/execute! conn
                   [(jdbc/drop-table-ddl :contest)])
    (jdbc/execute! conn
                   [(jdbc/drop-table-ddl :nomination)])))

(defn create-tables! [conn]
  (do
    (println "creating tables")
    (jdbc/execute! conn
                   [(jdbc/create-table-ddl :contest
                              [:id "BIGINT PRIMARY KEY AUTO_INCREMENT"]
                              [:name "VARCHAR NOT NULL"]
                              [:state "CHAR(1) NOT NULL"])])
    (jdbc/execute! conn
                   [(jdbc/create-table-ddl :nomination
                              [:id "BIGINT PRIMARY KEY AUTO_INCREMENT"]
                              [:name "VARCHAR"]
                              [:species "VARCHAR"])])))
  
(defn fixture-reset-db [test-function]
  (jdbc/with-db-transaction [conn db/db]
    (when (db/exists? conn "contest")
      (drop-tables! conn))
    (create-tables! conn)    
    (test-function)))

(use-fixtures :once fixture-reset-db)

(defn clear-tables! []
  (jdbc/with-db-transaction [conn db/db]
    (persist/delete-contest! conn)
    (persist/delete-nomination! conn)))

(defn insert-test-data! []
   (println "inserting test-data")
   ; contest
   (persist/create-contest! db/db {:name    "100"
                                   :state :closed})
   (persist/create-contest! db/db {:name    "101"
                                   :state :new
   ; nomination
   ;(persist/create-nomination! db/db {:name    "rororo"
                                      :species "rarara"}))
 
;;; Tests

#_(deftest test1 [])
  (clear-tables!)
;;  (insert-test-data!)
;;  (is (= (count (persist/read-contest db/db)) 2))
;;  (is (= (count (persist/read-nomination db/db)) 1)))

#_(deftest test2 [])
  (clear-tables!)
;;  (insert-test-data!)
;;  (is (= (count (persist/read-contest db/db)) 2))
;;  (is (= (count (persist/read-nomination db/db)) 1)))

(deftest test3 []
  (clear-tables!)
  (insert-test-data!))
  
  


(run-tests)
