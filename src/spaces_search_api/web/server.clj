(ns spaces-search-api.web.server
  (:require [taoensso.timbre :as timbre]
            [ring.adapter.jetty :refer [run-jetty]]  
            [com.stuartsierra.component :as component]))

(timbre/refer-timbre)

(defrecord WebServer [host port ring-handler]
  component/Lifecycle

  (start [this]
    (info "Starting web server")
    (if (:server this) 
      this
      (assoc this :server
             (run-jetty 
               (:handler ring-handler) 
               {:host host :port port :join? false}))))

  (stop [this]
    (info "Stopping web server")
    (if-not (:server this) 
      this
      (do (.stop (:server this))
          (dissoc this :server)))))

(defn web-server [host port]
  (component/using 
    (map->WebServer {:host host :port port})
    [:ring-handler]))

(defn web-server-test []
  (component/using 
    (map->WebServer {:host "127.0.0.1" :port 7777})
    [:ring-handler]))
