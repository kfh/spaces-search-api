(ns spaces-search-api.web.handler
  (:require [compojure.handler :as handler]
            [taoensso.timbre :as timbre]
            [com.stuartsierra.component :as component]  
            [ring.middleware.json :refer [wrap-json-body wrap-json-params wrap-json-response]]))

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
                 (wrap-json-body {:keywords? true})
                 (wrap-json-params)
                 (wrap-json-response)))))

  (stop [this]
    (info "Disabling ring handler")
    (if-not (:handler this)
      this
      (dissoc this :handler))))

(defn ring-handler []
  (component/using 
    (map->RingHandler {})
    [:api-routes]))
