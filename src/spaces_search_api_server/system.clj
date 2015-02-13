(ns spaces-search-api-server.system
  (:gen-class)
  (:require [taoensso.timbre :as timbre] 
            [com.stuartsierra.component :as component]   
            [spaces-search-api-server.storage.db :as db]
            [spaces-search-api-server.web.routes :as routes]
            [spaces-search-api-server.env.variables :as env]
            [spaces-search-api-server.web.server :as server]
            [spaces-search-api-server.service.queue :as queue]
            [spaces-search-api-server.web.handler :as handler]
            [spaces-search-api-server.logger.loggers :as logger]
            [spaces-search-api-server.service.subscriber :as subscriber]))

(timbre/refer-timbre)

(def config (-> "resources/spaces-search-api-server-conf.edn" 
                (slurp)
                (clojure.edn/read-string)))

(defn spaces-test-db []
  (component/system-map
    :es (db/elasticsearch-test)))

(defn spaces-test-system []
  (component/system-map
    :es (db/elasticsearch-test)
    :api-routes (routes/api-routes)
    :ring-handler (handler/ring-handler)
    :web-server (server/web-server-test)))

(defn spaces-system [config]
  (let [{:keys [web-host web-port]} config]
    (component/system-map
      :logger (logger/rolling-file-appender)
      :env (env/environment)
      :es (db/elasticsearch)
      :queue (queue/zeromq)
      :subscriber (subscriber/zeromq-subscriber)
      :api-routes (routes/api-routes)
      :ring-handler (handler/ring-handler)
      :web-server (server/web-server web-host web-port))))

(defn -main [& args]
  (component/start
    (spaces-system config))
  (info "Spaces search api up and running"))

