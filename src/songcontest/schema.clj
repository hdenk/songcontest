(ns songcontest.schema
  (:require [schema.core :as s]
            [schema.coerce :as s-coerce]
            [schema.utils :as s-utils]))

(defn coerce-and-validate [schema matcher data]
  (let [coercer (s-coerce/coercer schema matcher)
        result  (coercer data)]
    (if (s-utils/error? result)
      (throw (Exception. (format "Value does not match schema: %s"
                                 (s-utils/error-val result))))
      result)))

(def Contest-Phase (s/enum :new :nominate :rate :closed))

(def Contest
  {:name s/Str
   :phase Contest-Phase})

(defn coerce-params->contest [m]
  (coerce-and-validate Contest 
                       s-coerce/string-coercion-matcher
                       m))

(defn coerce-contest->db [m]
  (if (contains? m :phase) 
    (update-in m [:phase] (fn [x] (if (keyword? x) (name x) x)))
    m))

(defn coerce-db->contest [m]
  (if (contains? m :phase) 
    (update-in m [:phase] (fn [x] (if (string? x) (keyword x) x)))
    m))

(def Song
  {:artist s/Str
   :title s/Str})

(defn coerce-params->song [m]
  (coerce-and-validate Song 
                       s-coerce/string-coercion-matcher
                       m))

(defn coerce-song->db [m]
    m)

(defn coerce-db->song [m]
    m)

(def Nomination
  {:maedchen s/Num
   :contest s/Num
   :song s/Num})

(defn coerce-params->nomination [m]
  (coerce-and-validate Nomination 
                       s-coerce/string-coercion-matcher
                       m))

(defn coerce-nomination->db [m]
    m)

(defn coerce-db->nomination [m]
    m)
