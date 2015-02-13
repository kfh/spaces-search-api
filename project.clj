(defproject spaces-search-api-server "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :dependencies [[ring "1.3.2"]
                 [environ "1.0.0"]
                 [clj-http "1.0.1"]
                 [clj-time "0.8.0"]
                 [http-kit "2.1.19"]
                 [compojure "1.3.1"]
                 [liberator "0.12.2"]
                 [rubberlike "0.2.0"]
                 [ring-transit "0.1.2"]
                 [im.chit/ribol "0.4.0"]
                 [prismatic/schema "0.3.3"]
                 [org.zeromq/jeromq "0.3.4"]
                 [org.clojure/clojure "1.6.0"]
                 [im.chit/hara.common "2.1.7"] 
                 [com.taoensso/timbre "3.3.1"]
                 [clojurewerkz/elastisch "2.1.0"]  
                 [com.stuartsierra/component "0.2.2"] 
                 [com.cognitect/transit-clj "0.8.259"]
                 [io.clojure/liberator-transit "0.3.0"]
                 [com.keminglabs/zmq-async "0.1.0" :exclusions [com.keminglabs/jzmq]]]
  :plugins [[lein-ring "0.8.11"]]
  :profiles {:dev {:source-paths ["dev"]
                   :dependencies [[org.clojure/tools.namespace "0.2.7"]
                                  [org.clojure/java.classpath "0.2.2"]
                                  [javax.servlet/servlet-api "2.5"]
                                  [ring-mock "0.1.5"]]}
             :uberjar {:main spaces-search-api.system
                       :aot [spaces-search-api.system]}})
