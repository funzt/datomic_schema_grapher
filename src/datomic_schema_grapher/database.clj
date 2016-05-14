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
  "Returns a set of the names of all attributes that are used by
  entities referred to per attr"
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

