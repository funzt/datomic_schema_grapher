(ns datomic-schema-grapher.dot
  (:require [clojure.string :as str :refer (split join)]
            [hiccup.core :refer (html)]
            [dorothy.core :refer (subgraph node-attrs digraph dot show! save! graph-attrs)]))

(def light-grey "#808080")
(def edge-colors ["#441C14" "#15484C" "#257580" "#6E7D2C" "#CED796"])

(defn group-by-ident-ns
  [schema]
  (group-by (comp (some-fn namespace name) :db/ident) schema))

(defn is-a-ref?
  [attr]
  (= (:db/valueType attr) :db.type/ref))

(defn is-identifier?
  [attr]
  (= (:db/unique attr) :db.unique/identity))

(defn attr-row-label
  [attribute]
  (let [label (str (name (:db/ident attribute)))]
    (if (is-identifier? attribute)
      (html [:font {:color "red"} label])
      label)))

(defn- word-wrap
  "Word wrap a string after n characters.  Lines of a single word
  longer than n are not wrapped."
  [s n]
  (loop [[word & nwords :as words] (str/split s #" ")
         linec 0
         line []
         lines []]
    (if word
      (let [wordc (count word)
            next-linec (cond-> (+ linec wordc)
                         ;;; if there already was a word in this line,
                         ;;; we need to count a whitespace
                         (pos? linec) (inc))]
        (if (> next-linec (max n wordc))
          (recur words
                 0
                 []
                 (conj lines line))
          (recur nwords
                 next-linec
                 (conj line word)
                 lines)))
      (->> (conj lines line)
           (map (partial str/join " "))
           (interpose [:br])))))

(defn node-label
  [entity-name attributes]
  (html
   [:table {:port entity-name
            :border 0
            :color light-grey
            :cellborder 1
            :cellspacing 0}
    [:tr
     [:td {:bgcolor "gray"}
      entity-name]
     [:td
      "type"]
     [:td
      "card"]
     [:td
      "doc"]]
    (for [attribute attributes
          :let [attr-name (name (:db/ident attribute))]]
      [:tr
       [:td
        (attr-row-label attribute)]
       [:td
        (name (:db/valueType attribute))]
       [:td
        (if (= (:db/cardinality attribute)
               :db.cardinality/one)
          "1"
          "n")]
       [:td  (if (is-a-ref? attribute) {:port attr-name})
        (->> (str/split-lines (:db/doc attribute "-"))
             (map #(word-wrap % 30))
             (interpose [[:br]])
             (apply concat))]])]))

(defn dot-nodes
  "Create dorothy nodes for schema."
  [schema]
  (for [[entity-name attributes] (group-by-ident-ns schema)]
    [entity-name
     {:label (node-label entity-name
                         (sort-by :db/ident attributes))}]))

(defn circular-relationship?
  [[root dest-label _]]
  (= (namespace root) dest-label))


(defn shadow-ref-node
  "Dotted node used as destination for entities that refer to their
  own type because cycle arrows don't look good with ports"
  [[root dest-label _]]
  [(str dest-label "_ref") {:label dest-label
                            :shape "rectangle"
                            :style "dotted,rounded"
                            :color light-grey}])

(defn add-ref-colors
  [ref-nodes]
  (map (fn [[root-label dest-ref-label edge-attrs] color]
         [root-label dest-ref-label (merge edge-attrs
                                           {:color color
                                            :fontcolor color})])
       ref-nodes
       (cycle edge-colors)))

(defn dot-relationship
  [[root dest-label cardinality :as relationship]]
  (let [root-label (str (namespace root) ":" (name root))
        dest-ref-label (str dest-label "_ref")
        edge-attrs {;; :arrowhead (if (= cardinality "one") "tee" "crow")
                    :label (if (= cardinality "one")
                             "1:1"
                             "1:n")
                    }]
    (if (circular-relationship? relationship)
      [root-label dest-ref-label edge-attrs]
      [root-label (str dest-label ":" dest-label) edge-attrs])))

(defn dot-relationships
  [relationships]
  (concat (add-ref-colors (map dot-relationship relationships))
          (map shadow-ref-node (filter circular-relationship? relationships))))

(defn to-digraph-statements [schema relationships]
  (concat [(node-attrs {:shape "plaintext"})]
          (dot-nodes schema)
          (when (not-empty relationships)
            (dot-relationships relationships))))

(defn to-dot
  [schema relationships]
  (dot (digraph (to-digraph-statements schema relationships))))

(defn show
  [schema relationships]
  (show! (to-dot schema relationships)))
