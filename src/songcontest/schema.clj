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

(defn coerce-contest [m]
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

(def Nomination
  {:name s/Str
   :species s/Str})