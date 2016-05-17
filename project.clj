(defproject org.funzt/datomic-schema-grapher "0.1.0-SNAPSHOT"
  :description "A library and lein plugin for graphing the datomic schema."
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [dorothy "0.0.6"]
                 [hiccup "1.0.5"]]
  :profiles {:provided
             {:dependencies [;; user must provide her own datomic
                             [com.datomic/datomic-free "0.9.5350"]]}
             :dev
             {:source-paths ["src" "dev"]
              :dependencies [[org.clojure/tools.namespace "0.2.11"]]}

             :repl
             {:repl-options {:init-ns dev}}})
