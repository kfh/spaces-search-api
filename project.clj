(defproject spaces-search-api "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :dependencies [[ring "1.4.0"]
                 [environ "1.0.1"]
                 [liberator "0.13"]
                 [clj-http "2.0.0"]
                 [clj-time "0.11.0"]
                 [http-kit "2.1.19"]
                 [compojure "1.4.0"]
                 [rubberlike "0.2.1"]
                 [reloaded.repl "0.2.0"]
                 [im.chit/ribol "0.4.1"]
                 [prismatic/schema "1.0.4"]
                 [org.clojure/clojure "1.8.0"]
                 [com.taoensso/timbre "4.1.1"]
                 [im.chit/hara.common "2.2.11"] 
                 [org.immutant/messaging "2.1.0"]
                 [metosin/compojure-api "0.24.3"]
                 [com.stuartsierra/component "0.2.3"] 
                 [com.cognitect/transit-clj "0.8.281"]
                 [org.elasticsearch/elasticsearch "1.7.2"]
                 [org.hornetq/hornetq-jms-server "2.3.17.Final"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [clojurewerkz/elastisch "2.1.0" :exclusions [org.elasticsearch/elasticsearch]]]  
  :profiles {:dev {:source-paths ["dev"]
                   :dependencies [[org.clojure/tools.namespace "0.2.10"]
                                  [org.clojure/java.classpath "0.2.2"]
                                  [ring-mock "0.1.5"]   
                                  [javax.servlet/servlet-api "2.5"]]}
             :uberjar {:main spaces-search-api.system
                       :aot [spaces-search-api.system]}})
