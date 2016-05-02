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

;;; Promotion

(def Promotion
 {:name s/Str
  :species s/Str})

(defn create-promotion!
  ([db m]
   (s/validate Contest m)
   (let [result (jdbc/insert! db :promotion m)
         id (get (first result) (keyword "scope_identity()"))]
     id)))

(defn read-promotion
  ([db]
   (jdbc/query db ["select * from promotion"]))
  ([db id]
   (first (jdbc/query db [(str "select * from promotion\n"
                               "where id = ?") id]))))

(defn update-promotion!
  [db id m]
  (jdbc/update! db :promotion m ["id = ?" id]))

(defn delete-promotion!
  ([db]
   (jdbc/execute! db ["delete from promotion"]))
  ([db id]
   (jdbc/delete! db :promotion ["id = ?" id])))

;;; Initialization

(defn insert-samples! [db]
  (println "inserting some contests")
  (do
    (create-contest! db {:name    "Painted-snipe"
                         :species "Rostratulidae"})
    (create-contest! db {:name    "Yellow-backed duiker"
                         :species "Cephalophus silvicultor"})
    (create-contest! db {:name    "Aardwolf"
                         :species "Proteles cristata"})
    (create-contest! db {:name    "Gnu"
                         :species "Dbochaetes gnou"})
    (create-contest! db {:name    "Atlantic salmon"
                         :species "Salmo salar"})
    (create-promotion! db {:name    "rororo"
                           :species "rarara"})))

(defn init
  [db]
  (jdbc/with-db-transaction [conn db]
    (if-not (db/exists? conn "contest")
      (do
        (println "creating contest table")
        (jdbc/execute! conn
                       [(jdbc/create-table-ddl :contest
                                               [:id "bigint primary key auto_increment"]
                                               [:name "varchar"]
                                               [:species "varchar"])])
        (println "creating promotion table")
        (jdbc/execute! conn
                       [(jdbc/create-table-ddl :promotion
                                               [:id "bigint primary key auto_increment"]
                                               [:name "varchar"]
                                               [:species "varchar"])])
        (insert-samples! conn))
      (println "table contest already exists"))))
