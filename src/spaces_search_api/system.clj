(ns spaces-search-api.system
  (:gen-class)
  (:require [clojure.edn :as edn]
            [taoensso.timbre :as timbre] 
            [spaces-search-api.storage.db :as db]
            [spaces-search-api.env.variables :as env]
            [spaces-search-api.web.server :as server]
            [com.stuartsierra.component :as component]   
            [spaces-search-api.service.queue :as queue]
            [spaces-search-api.web.handler :as handler]
            [spaces-search-api.logger.loggers :as logger]
            [spaces-search-api.service.subscriber :as subscriber]
            [reloaded.repl :refer [system init start stop go reset]]))

(timbre/refer-timbre)

(def config (-> "resources/spaces-search-api-conf.edn" 
                (slurp)
                (edn/read-string)))

(defn spaces-test-db []
  (component/system-map
    :es (db/elasticsearch-test)))

(defn spaces-test-system []
  (component/system-map
    :es (db/elasticsearch-test)
    :ring-handler (handler/ring-handler)
    :web-server (server/web-server-test)))

(defn spaces-system [config]
  (let [{:keys [web-host web-port]} config]
    (component/system-map
      :logger (logger/rolling-file-appender)
      :env (env/environment)
      :es (db/elasticsearch)
      :hornetq-geolocations (queue/hornetq-geolocations)
      :geolocations-subscriber (subscriber/geolocations-subscriber)
      :ring-handler (handler/ring-handler)
      :web-server (server/web-server web-host web-port))))

(defn -main []
  (reloaded.repl/set-init! #(spaces-system config))
  (go)
  (info "Spaces search api up and running"))

