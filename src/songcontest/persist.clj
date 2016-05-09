(ns songcontest.persist
  (:refer-clojure :exclude [read])
  (:require [clojure.java.jdbc :as jdbc]
            [songcontest.schema :as schema]
            [songcontest.db :as db]))

;;; Contest

(defn create-contest!
  ([db m]
   (let [result (jdbc/insert! db :contest  (schema/coerce-contest->db m))
         id (get (first result) (keyword "scope_identity()"))]
        id)))

(defn read-contest
  ([db]
   (map (fn [m] (schema/coerce-db->contest m)) (jdbc/query db ["select * from contest"])))
  ([db id]
   (schema/coerce-db->contest (first (jdbc/query db [(str "select * from contest\n"
                               "where id = ?") id])))))

(defn update-contest!
  [db id m]
  (jdbc/update! db :contest (schema/coerce-contest->db m) ["id = ?" id]))

(defn delete-contest!
  ([db]
   (jdbc/execute! db ["delete from contest"]))
  ([db id]
   (jdbc/delete! db :contest ["id = ?" id])))

;;; Nomination

(defn create-nomination!
  ([db m]
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

