(ns spaces-search-api.system
  (:gen-class)
  (:require [taoensso.timbre :as timbre] 
            [spaces-search-api.storage.db :as db]
            [spaces-search-api.web.routes :as routes]
            [spaces-search-api.web.handler :as handler]
            [spaces-search-api.web.server :as server]
            [com.stuartsierra.component :as component]))

(timbre/refer-timbre)

(defn spaces-test-db []
  (component/system-map
      :es (db/es-test)))

(defn spaces-test-system []
  (component/system-map
    :es (db/es)
    :api-routes (routes/api-routes)
    :ring-handler (handler/ring-handler)
    :web-server (server/web-server-test)))

(defn spaces-system [config]
  (let [{:keys [db-host db-ports http-host http-port]} config]
    (component/system-map
      :es (db/es db-host db-ports)
      :api-routes (routes/api-routes)
      :ring-handler (handler/ring-handler)
      :web-server (server/web-server http-host http-port))))

(def system nil)

(defn init []
  (alter-var-root
    #'system
    (constantly
      (spaces-system
        {:db-host "127.0.0.1"
         :db-ports {:web 9200 :native 9300}
         :http-host "127.0.0.1"
         :http-port 4444}))))

(defn start []
  (alter-var-root #'system component/start))

(defn stop []
  (alter-var-root #'system
    (fn [s] (when s (component/stop s)))))

(defn -main [& args]
  (init)
  (start)
  (info "Spaces search api up and running"))

