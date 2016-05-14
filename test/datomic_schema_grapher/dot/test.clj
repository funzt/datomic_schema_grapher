(ns datomic-schema-grapher.dot.test
  (:require [clojure.test :refer :all]
            [datomic-schema-grapher.dot :refer :all]))

(def schema1 [{:db/ident :entity1/attr1
               :db/cardinality :db.cardinality.one
               :db/valueType :db.type/string}
              {:db/ident :entity1/attr2
               :db/cardinality :db.cardinality.many
               :db/valueType :db.type/string}
              {:db/ident :entity2/attr1
               :db/cardinality :db.cardinality.many
               :db/valueType :db.type/string}
              {:db/ident :entity2/entity1
               :db/cardinality :db.cardinality.one
               :db/valueType :db.type/ref}])

(deftest test-group-by-ident-ns
  (testing "group-by-ident-ns"
    (let [entities (group-by-ident-ns schema1)]
      (are [x y] (= (map :db/ident x) y)
           (entities "entity1") '(:entity1/attr1 :entity1/attr2)
           (entities "entity2") '(:entity2/attr1 :entity2/entity1)))))

(def attr-ref {:db/ident :entity1/reference
               :db/cardinality :db.cardinality.one
               :db/valueType :db.type/ref})

(def attr-string {:db/ident :entity1/attr1
                  :db/cardinality :db.cardinality.one
                  :db/valueType :db.type/string})

(def attr-unique {:db/ident :entity1/attr2
                  :db/cardinality :db.cardinality.one
                  :db/unique :db.unique/identity
                  :db/valueType :db.type/string})

(deftest test-is-a-ref?
  (testing "is-a-ref?"
    (are [x y] (= (is-a-ref? x) y)
         attr-ref true
         attr-string false)))

(deftest test-is-unique?
  (testing "is-unique?"
    (are [x y] (= (is-unique? x) y)
         attr-unique true
         attr-string false)))

(deftest test-attr-row-label
  (testing "Attribute rows are generated properly"
    (are [x y] (= (attr-row-label x) y)
         attr-unique "<font color=\"red\">attr2 : string</font>"
         attr-string "attr1 : string")))

(deftest test-is-circular-relationship
  (testing "Circular relationships are detected"
    (are [x y] (= (circular-relationship? x) y)
         '(:entity1/multi "entity1" "many") true
         '(:entity1/entity2 "entity2" "one") false)))

(deftest testing-add-ref-colors
  (testing "Edge colors are properly selected for a given relationship"
    (is (= (add-ref-colors [["entity1:multi" "entity1_ref" {:arrowhead "crow"}]
                            ["entity1:self" "entity1_ref" {:arrowhead "crow"}]])
           [["entity1:multi" "entity1_ref" {:color "#441C14", :arrowhead "crow"}]
            ["entity1:self" "entity1_ref" {:color "#15484C", :arrowhead "crow"}]]))))

(deftest test-node-label
  (testing "node-label"
    (are [x y] (= (node-label [x]) y)
         attr-ref "<table border=\"0\" cellborder=\"1\" cellspacing=\"0\" color=\"#808080\" port=\"entity1\"><tr><td bgcolor=\"gray\">entity1</td></tr><tr><td port=\"reference\">reference : ref</td></tr></table>"
         attr-string "<table border=\"0\" cellborder=\"1\" cellspacing=\"0\" color=\"#808080\" port=\"entity1\"><tr><td bgcolor=\"gray\">entity1</td></tr><tr><td>attr1 : string</td></tr></table>")))

(deftest test-dot-relationship
  (testing "Relationships are properly mapped"
    (are [x y] (= (dot-relationship x) y)
         '(:entity1/multi "entity2" "many") ["entity1:multi" "entity2:entity2" {:arrowhead "crow"}]
         '(:entity1/self "entity1" "one") ["entity1:self" "entity1_ref" {:arrowhead "tee"}])))

