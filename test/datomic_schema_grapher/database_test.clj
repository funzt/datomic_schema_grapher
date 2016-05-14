(ns datomic-schema-grapher.database-test
  (:require [clojure.test :refer :all]
            [utils.datomic-helpers :as util]
            [datomic-schema-grapher.database :as db]))

(def uri "datomic:mem://database-test")
(def db-schema "/test/datomic_schema_grapher/database/schema.edn")
(def fixtures "/test/datomic_schema_grapher/database/fixtures.edn")

(def ^:dynamic *db* "The DB under test" nil)

(use-fixtures :each
  (fn [t]
    (with-bindings {#'*db* (-> uri
                               (doto (util/prepare-database! db-schema
                                                             fixtures))
                               (util/database))}
      (t))
    (util/delete-database! uri)))

(deftest test-schema
  (testing "Returns all attribute entities of the database"
    (is (= (into #{}
                 (map :db/ident)
                 (db/schema *db*))
           #{:entity1/multi
             :entity1/entity2
             :entity2/entity1
             :entity2/attr
             :entity1/self}))))

(deftest test-referencing-namespaces
  (testing "Returns a collection of referenced namespaces"
    (are [x y] (= (db/ref-entities *db* x) y)
      :entity1/self #{"entity1"}
      :entity1/multi #{"entity1" "entity2"}
      :entity1/entity2 #{"entity2"}
      :entity2/entity1 #{"entity1"})))

(deftest test-references
  (testing "Returns a mapping of all references in the database"
    (is (= (set (db/references *db*))
           #{[:entity1/multi "entity1" "many"]
             [:entity1/multi "entity2" "many"]
             [:entity1/entity2 "entity2" "one"]
             [:entity2/entity1 "entity1" "many"]
             [:entity1/self "entity1" "many"]}))))
