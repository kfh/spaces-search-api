(ns spaces-search-api-server.web.handler
  (:require [compojure.handler :as handler]
            [taoensso.timbre :as timbre]
            [com.stuartsierra.component :as component]  
            [ring.middleware.transit :refer [wrap-transit-params]]))

(timbre/refer-timbre)

(defrecord RingHandler [api-routes]
  component/Lifecycle

  (start [this]
    (info "Enabling ring handler")
    (if (:handler this)
      this 
      (assoc this :handler 
             (-> api-routes 
                 :routes 
                 (handler/api)
                 (wrap-transit-params)))))

  (stop [this]
    (info "Disabling ring handler")
    (if-not (:handler this)
      this
      (dissoc this :handler))))

(defn ring-handler []
  (component/using 
    (map->RingHandler {})
    [:api-routes]))
