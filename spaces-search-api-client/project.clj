(defproject spaces-search-api-client "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :dependencies [[clj-time "0.8.0"]  
                 [http-kit "2.1.16"]
                 [im.chit/ribol "0.4.0"]
                 [im.chit/hara.common "2.1.7"]  
                 [org.clojure/clojure "1.6.0"]
                 [com.stuartsierra/component "0.2.2"]  
                 [com.cognitect/transit-clj "0.8.259"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]]
  :profiles {:test {:dependencies [[spaces-search-api-server "0.1.0-SNAPSHOT"]]}})
