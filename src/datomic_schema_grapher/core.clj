(ns datomic-schema-grapher.core
  (:require [datomic.api :as d]
            [datomic-schema-grapher.database :as db]
            [datomic-schema-grapher.dot :as dot])
  (:import javax.swing.JFrame))

(defn graph-datomic-db
  "Render a nice graphviz of your Datomic schema"
  [db & {:keys [save-as no-display exit-on-close]}]
  (let [schema (db/schema db)
        references (db/references db)]
    (if save-as
      (spit save-as (dot/to-dot schema references)))
    (when-not no-display
      (let [jframe (dot/show schema references)]
        (when exit-on-close
          ;; mainly for use with the lein plugin
          (.setDefaultCloseOperation jframe JFrame/EXIT_ON_CLOSE)
          (while true (Thread/sleep 500)))))))

(defn graph-datomic
  "Like graph-datomic-db, but takes an uri instead of db."
  {:deprecated true}
  [uri & opts]
  (apply graph-datomic-db (d/db (d/connect uri)) opts))
