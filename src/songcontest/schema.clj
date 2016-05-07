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

(def Contest-Phase-DB (s/enum /N /O /R /C))

(def Contest
  {:name s/Str
   :phase Contest-Phase})

(def Contest-DB
  {:name s/Str
   :phase Contest-Phase-DB})

(def Nomination
  {:name s/Str
   :species s/Str})

(defn RequestParams->DatabaseParams
  [{:keys [comment-id comment-body] :as request-params}]
  {:id comment-id
   :body comment-body})

(def Contest->Database-coercer
  (coerce/coercer Contest-DB {Contest-Phase-DB RequestParams->DatabaseParams
                                  s/Int #(Integer/parseInt %)}))


