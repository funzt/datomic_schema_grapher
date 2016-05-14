(ns datomic-schema-grapher.database
  (:require [datomic.api :as d])
  (:import java.util.Date))

(defn schema
  "Returns all user defined datomic attribute as entities."
  [db]
  (->> (d/q '[:find [?attr ...]
              :in $ ?date-zero
              :where
              [_ :db.install/attribute ?attr ?tx]
              (not [?tx :db/txInstant ?date-zero])]
            db
            (Date. 0))
       (map #(d/entity db %))))

(defn ref-entities
  "Returns a set of the namespaces of all attributes that are used by
  entities referred to per attr."
  [db attr]
  (into #{}
        (map namespace)
        (d/q '[:find [?ref-name ...]
               :in $ ?attr
               :where
               [_ ?attr ?ref]
               [?ref ?ref-attr]
               [?ref-attr :db/ident ?ref-name]]
             db
             attr)))

(defn references
  "Return tuples [attr-ident referred-ns card] where referred-ns is
  the namespace of at least one attribute that an entity has that is
  referenced via attr-ident."
  [db]
  (for [{:keys [db/ident db/valueType db/cardinality]} (schema db)
        :when (= valueType :db.type/ref)
        referred-ns (ref-entities db ident)]
    [ident referred-ns (name cardinality)]))

