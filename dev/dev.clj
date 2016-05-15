(ns dev
  (:require [datomic.api :as d]
            [clojure.java.io :as io]
            [datomic-schema-grapher.core :as dsgc]
            [datomic-schema-grapher.database :as db]
            [datomic-schema-grapher.dot :as dot]
            [clojure.tools.namespace.repl :as r])
  (:import [datomic Util]))

(r/set-refresh-dirs "src/utils"
                    "src/datomic-schema-grapher"
                    "test"
                    "dev")

(def schema
  (Util/readAll (-> "datomic_schema_grapher/database/schema.edn"
                    (io/resource)
                    (io/reader))))

(def fixtures
  (Util/readAll (-> "datomic_schema_grapher/database/fixtures.edn"
                    (io/resource)
                    (io/reader))))

(defn create-test-db [url schema]
  (let [db0 (-> (doto url
                  (d/delete-database)
                  (d/create-database))
                (d/connect)
                (d/db))]
    (reduce (fn [db tx]
              (:db-after (d/with db tx)))
            db0
            schema)))

(def db-1
  (create-test-db "datomic:mem://test-db-1"
                  (concat schema fixtures)))

(def db-2
  (create-test-db
   "datomic:mem://test-db-1"
   [ ;; Schema
    [{:db/id (d/tempid :db.part/db)
      :db/ident :shop/name
      :db/valueType :db.type/string
      :db/cardinality :db.cardinality/one
      :db.install/_attribute :db.part/db}

     {:db/id (d/tempid :db.part/db)
      :db/ident :shop/customers
      :db/valueType :db.type/ref
      :db/cardinality :db.cardinality/many
      :db.install/_attribute :db.part/db}

     {:db/id (d/tempid :db.part/db)
      :db/ident :shop/owner
      :db/valueType :db.type/ref
      :db/cardinality :db.cardinality/one
      :db.install/_attribute :db.part/db}

     {:db/id (d/tempid :db.part/db)
      :db/ident :customer/email
      :db/valueType :db.type/string
      ;; :db/unique :db.unique/identity
      :db/cardinality :db.cardinality/one
      :db.install/_attribute :db.part/db}]
    ;; Some-data
    [{:db/id (d/tempid :db.part/user -1)
      :customer/email "customer1@gmail.com"}
     {:db/id (d/tempid :db.part/user -2)
      :customer/email "customer2@yahoo.com"}
     {:db/id (d/tempid :db.part/user -3)
      :customer/email "customer3@hotmail.com"}
     {:db/id (d/tempid :db.part/user)
      :shop/name "Permanent Shop"
      :shop/owner (d/tempid :db.part/user -3)
      :shop/customers [(d/tempid :db.part/user -1)
                       (d/tempid :db.part/user -2)]}
     {:db/id (d/tempid :db.part/user)
      :shop/name "Orange Shop"
      :shop/customers [(d/tempid :db.part/user -1)
                       (d/tempid :db.part/user -2)
                       (d/tempid :db.part/user -3)]}
     {:db/id (d/tempid :db.part/user)
      :shop/name "Tasteful Shop"}]]))


#_(r/refresh)
