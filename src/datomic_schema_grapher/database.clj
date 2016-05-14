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
  "Returns all entities the references a given datomic attribute."
  [db attr-name]
  (->>  (d/q '[:find ?ref-name
               :in $ ?attr-name
               :where
               [_ ?attr-name ?ref]
               [?ref ?ref-attr]
               [?ref-attr :db/ident ?ref-name]]
             db
             attr-name)
       (apply concat)
       (map namespace)
       (set)))

(defn references
  [db]
  (let [ref-attrs (->> (schema db)
                       (group-by :db/valueType)
                       :db.type/ref)]
    (->> (for [ref-attr ref-attrs]
           (interleave (repeat (:db/ident ref-attr))
                       (ref-entities db (:db/ident ref-attr))
                       (repeat (name (:db/cardinality ref-attr)))))
         flatten
         (partition 3))))

