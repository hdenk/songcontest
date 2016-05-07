(ns songcontest.test.coerce
  (:require [clojure.data.json :as json]
            [clojure.instant :refer [read-instant-date]]
            [clojure.java.io :as io]
            [schema.coerce :as coerce]
            [schema.core :as s]
            [schema.utils :as s-utils]
            [schema.spec.core :as spec])
  (:import java.util.Date))

(def Config
  {:users  #{s/Str}
   :after  (s/maybe Date)
   :format (s/enum :txt :json)})

(def datetime-regex #"\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}Z")

(defn datetime-matcher [schema]
  ;(println schema)
  (when (= Date schema)
    (coerce/safe
      (fn [x]
        (if (and (string? x) (re-matches datetime-regex x))
          (read-instant-date x)
          x)))))

#_(defn contest-state-matcher [schema] nil)

#_(defn keyword-enum-matcher [schema])
  (when (or (and (instance?       schema.core.EnumSchema                     schema)
                 (every? keyword? (.-vs ^schema.core.EnumSchema schema)))
            (and (instance?       schema.core.EqSchema                   schema)
                 (keyword? (.-v ^schema.core.EqSchema schema))))
    string->keyword)


(def config-matcher
  (coerce/first-matcher [datetime-matcher coerce/json-coercion-matcher]))

(def config-file-name "config.json")

(defn load-config-file []
  (-> config-file-name
      (io/reader)
      (json/read :key-fn keyword)))

(defn coerce-and-validate [schema matcher data]
  (let [coercer (coerce/coercer schema matcher)
        result  (coercer data)]
    (if (s-utils/error? result)
      (throw (Exception. (format "Value does not match schema: %s"
                                 (s-utils/error-val result))))
      result)))

(println "!!!hiho!!!")
(->> (load-config-file)
     (coerce-and-validate Config config-matcher))

(defn walk-demo [schema]
  (spec/run-checker
   (fn [s params]
     (let [walk (spec/checker (s/spec s) params)]
       (fn [x]
         (let [result (walk x)]
           (printf "%s | checking %s against %s\n"
                   (if (s-utils/error? result) "FAIL" "PASS")
                   x (s/explain s))
           result))))
   true
   schema))

((walk-demo {:a Long (s/optional-key :b) String}) {:a 3 :b "Hello"})

((walk-demo Config) (load-config-file))

(def Contest
 {:name s/Str
  :state (s/enum :new :nominate :rate :closed)})

((walk-demo Contest) {:name "Contest1" :state \N})

(class \N)
