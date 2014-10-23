(ns spaces-search-api.system
  (:gen-class)
  (:require [taoensso.timbre :as timbre] 
            [spaces-search-api.storage.db :as db]
            [spaces-search-api.web.routes :as routes]
            [spaces-search-api.env.variables :as env]
            [spaces-search-api.web.server :as server]
            [spaces-search-api.web.handler :as handler]
            [com.stuartsierra.component :as component]))

(timbre/refer-timbre)

(defn spaces-test-db []
  (component/system-map
    :env (env/environment)  
    :es (db/elasticsearch)))

(defn spaces-test-system []
  (component/system-map
    :env (env/environment)
    :es (db/elasticsearch)
    :api-routes (routes/api-routes)
    :ring-handler (handler/ring-handler)
    :web-server (server/web-server-test)))

(defn spaces-system [config]
  (let [{:keys [http-host http-port]} config]
    (component/system-map
      :env (env/environment)
      :es (db/elasticsearch)
      :api-routes (routes/api-routes)
      :ring-handler (handler/ring-handler)
      :web-server (server/web-server http-host http-port))))

(def system nil)

(defn init []
  (alter-var-root
    #'system
    (constantly
      (spaces-system
        {:http-host "127.0.0.1"
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

