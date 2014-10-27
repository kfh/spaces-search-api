(ns spaces-search-api.system
  (:gen-class)
  (:require [taoensso.timbre :as timbre] 
            [spaces-search-api.storage.db :as db]
            [spaces-search-api.web.routes :as routes]
            [spaces-search-api.env.variables :as env]
            [spaces-search-api.web.server :as server]
            [spaces-search-api.web.handler :as handler]
            [spaces-search-api.logger.loggers :as logger]
            [com.stuartsierra.component :as component]))

(timbre/refer-timbre)

(def config (-> "resources/spaces-search-api-conf.edn" 
                (slurp)
                (clojure.edn/read-string)))

(defn spaces-test-db []
  (component/system-map
    :env (env/environment)  
    :es (db/elasticsearch)))

(defn spaces-test-system []
  (component/system-map
    :logger (logger/rolling-file-appender)
    :env (env/environment)
    :es (db/elasticsearch)
    :api-routes (routes/api-routes)
    :ring-handler (handler/ring-handler)
    :web-server (server/web-server-test)))

(defn spaces-system [config]
  (let [{:keys [web-host web-port]} config]
    (component/system-map
      :logger (logger/rolling-file-appender)
      :env (env/environment)
      :es (db/elasticsearch)
      :api-routes (routes/api-routes)
      :ring-handler (handler/ring-handler)
      :web-server (server/web-server web-host web-port))))

(defn -main [& args]
  (component/start
    (spaces-system config))
  (info "Spaces search api up and running"))

