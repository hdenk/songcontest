(ns songcontest.persist
  (:refer-clojure :exclude [read])
  (:require [clojure.java.jdbc :as jdbc]
            [schema.core :as s]
            [songcontest.db :as db]))

;;; Contest

(def Contest
 {:name s/Str
  :state (s/enum :new :nominate :rate :closed)})

(defn create-contest!
  ([db m]
   (s/validate Contest m)
   (let [result (jdbc/insert! db :contest (assoc m :state \N))
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

