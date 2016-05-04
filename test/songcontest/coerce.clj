(ns soncontest.coerce
  (:require [clojure.data.json :as json]
            [clojure.instant :refer [read-instant-date]]
            [clojure.java.io :as io]
            [schema.coerce :as coerce]
            [schema.core :as s]
            [schema.utils :as s-utils])
  (:import java.util.Date))

(def Config
  {:users  #{s/Str}
   :after  (s/maybe Date)
   :format (s/enum :txt :json)})

(def config-file-name "config.json")

(defn load-config-file []
  (-> config-file-name
      (io/reader)
      (json/read :key-fn keyword)))

(->> (load-config-file)
     (s/validate Config))
