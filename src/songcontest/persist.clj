(ns songcontest.persist
  (:refer-clojure :exclude [read])
  (:require [clojure.java.jdbc :as jdbc]
            [songcontest.schema :as schema]
            [songcontest.db :as db]))

(defn timestamp-created_at [m]
  (assoc m :created_at (java.sql.Timestamp. (.getTime (java.util.Date.)))))

(defn timestamp-modified_at [m]
  (assoc m :modified_at (java.sql.Timestamp. (.getTime (java.util.Date.)))))

;;; Contest

(defn create-contest!
  ([db m]
   (let [result (jdbc/insert! db :contest (schema/coerce-contest->db (timestamp-created_at m)))
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
  (jdbc/update! db :contest (schema/coerce-contest->db (timestamp-modified_at m)) ["id = ?" id]))

(defn delete-contest!
  ([db]
   (jdbc/execute! db ["delete from contest"]))
  ([db id]
   (jdbc/delete! db :contest ["id = ?" id])))

;;; Song

(defn create-song!
  ([db m]
   (let [result (jdbc/insert! db :song  (schema/coerce-song->db (timestamp-created_at m)))
         id (get (first result) (keyword "scope_identity()"))]
        id)))

(defn read-song
  ([db]
   (map (fn [m] (schema/coerce-db->song m)) (jdbc/query db ["select * from song"])))
  ([db id]
   (schema/coerce-db->song (first (jdbc/query db [(str "select * from song\n"
                                                       "where id = ?") id])))))

(defn update-song!
  [db id m]
  (jdbc/update! db :song (schema/coerce-song->db (timestamp-modified_at m)) ["id = ?" id]))

(defn delete-song!
  ([db]
   (jdbc/execute! db ["delete from song"]))
  ([db id]
   (jdbc/delete! db :song ["id = ?" id])))

;;; Nomination

(defn create-nomination!
  ([db m]
   (let [result (jdbc/insert! db :nomination (schema/coerce-nomination->db (timestamp-created_at m)))
         id (get (first result) (keyword "scope_identity()"))]
     id)))

(defn read-nomination
  ([db]
   (map (fn [m] (schema/coerce-db->nomination m)) (jdbc/query db ["select * from nomination"])))
  ([db id]
   (schema/coerce-db->nomination (first (jdbc/query db [(str "select * from nomination\n"
                                                             "where id = ?" id)])))))

(defn update-nomination!
  [db id m]
  (jdbc/update! db :nomination (timestamp-modified_at m) ["id = ?" id]))

(defn delete-nomination!
  ([db]
   (jdbc/execute! db ["delete from nomination"]))
  ([db id]
   (jdbc/delete! db :nomination ["id = ?" id])))

