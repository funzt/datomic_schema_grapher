(defproject datomic-schema-grapher "0.0.1"
  :description "A library and lein plugin for graphing the datomic schema."
  :url "https://github.com/felixflores/datomic_schema_grapher"
  :license {:name "MIT License"
            :url "http://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [dorothy "0.0.6"]
                 [hiccup "1.0.5"]]
  :profiles {:provided
             {:dependencies [;; user must provide her own datomic
                             [com.datomic/datomic-free "0.9.5350"]]}})
