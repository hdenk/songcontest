(ns songcontest.persist
  (:refer-clojure :exclude [read])
  (:require [clojure.java.jdbc :as jdbc]
            [schema.core :as s]
            [songcontest.db :as db]))

;;; Contest

(def Contest
 {:name s/Str
  :species s/Str})

(defn create-contest!
  ([db m]
   (s/validate Contest m)
   (let [result (jdbc/insert! db :contest m)
         id (get (first result) (keyword "scope_identity()"))]
     id)))

(defn read-contest
  ([db]
   (jdbc/query db ["select * from contest"]))
  ([db id]
   (first (jdbc/query db [(str "select * from contest\n"
                               "where id = ?") id]))))

(defn update-contest!
  [db id m]
  (jdbc/update! db :contest m ["id = ?" id]))

(defn delete-contest!
  ([db]
   (jdbc/execute! db ["delete from contest"]))
  ([db id]
   (jdbc/delete! db :contest ["id = ?" id])))

;;; Nomination

(def Nomination
 {:name s/Str
  :species s/Str})

(defn create-nomination!
  ([db m]
   (s/validate Contest m)
   (let [result (jdbc/insert! db :nomination m)
         id (get (first result) (keyword "scope_identity()"))]
     id)))

(defn read-nomination
  ([db]
   (jdbc/query db ["select * from nomination"]))
  ([db id]
   (first (jdbc/query db [(str "select * from nomination\n"
                               "where id = ?") id]))))

(defn update-nomination!
  [db id m]
  (jdbc/update! db :nomination m ["id = ?" id]))

(defn delete-nomination!
  ([db]
   (jdbc/execute! db ["delete from nomination"]))
  ([db id]
   (jdbc/delete! db :nomination ["id = ?" id])))

;;; Initialization

(defn insert-samples! [db]
  (do
   (println "inserting some contests")
   (create-contest! db {:name    "100"
                        :state \C})
   (create-contest! db {:name    "101"
                        :state \N})
   (println "inserting some nominations")
   (create-nomination! db {:name    "rororo"
                           :species "rarara"})))

(defn init
  [db]
  (jdbc/with-db-transaction [conn db]
    (if-not (db/exists? conn "contest")
      (do
        (println "creating contest table")
        (jdbc/execute! conn
                       [(jdbc/create-table-ddl :contest
                                               [:id "BIGINT PRIMARY KEY AUTO_INCREMENT"]
                                               [:name "VARCHAR NOT NULL"]
                                               [:state "CHAR(1) NOT NULL"])])
        (println "creating nomination table")
        (jdbc/execute! conn
                       [(jdbc/create-table-ddl :nomination
                                               [:id "BIGINT PRIMARY KEY AUTO_INCREMENT"]
                                               [:name "VARCHAR"]
                                               [:species "VARCHAR"])])
        (insert-samples! conn))
      (println "table contest already exists"))))
