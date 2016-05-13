(ns dev
  (:require [datomic.api :as d]
            [clojure.java.io :as io])
  (:import [datomic Util]))

(def schema
  (Util/readAll (-> "datomic_schema_grapher/database/schema.edn"
                    (io/resource)
                    (io/reader))))

(def fixtures
  (Util/readAll (-> "datomic_schema_grapher/database/fixtures.edn"
                    (io/resource)
                    (io/reader))))

(defn create-basis-db []
  (let [db0 (-> (doto "datomic:mem://basis-db"
                  (d/delete-database)
                  (d/create-database))
                (d/connect)
                (d/db))]
    (reduce (fn [db tx]
              (:db-after (d/with db tx)))
            db0
            (concat schema fixtures))))

(def basis-db
  (create-basis-db))
