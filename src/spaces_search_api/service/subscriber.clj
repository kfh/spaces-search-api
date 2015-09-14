(ns spaces-search-api.service.subscriber
  (:require [taoensso.timbre :as timbre]
            [com.stuartsierra.component :as component]
            [clojure.core.async :refer [<!! close! thread]]
            [spaces-search-api.service.locations :as service]))

(timbre/refer-timbre)

(defrecord GeolocationsSubscriber [es hornetq-geolocations]
  component/Lifecycle

  (start [this]
    (info "Starting geolocations subscriber")
    (if (:subscriber this)
      this
      (let [{:keys [conn index m-type]} es
            sub-out (:sub-out hornetq-geolocations)] 
        (thread
          (while true
            (when-let [geolocations (<!! sub-out)]
              (->> geolocations
                   (spy :info "Preparing to index: ") 
                   (service/index-locations conn index m-type)
                   (spy :info "Index result: ")))))
        (assoc this :subscriber sub-out))))

  (stop [this]
    (info "Stopping geolocations subscriber")
    (if-not (:subscriber this)
      this
      (do
        (close! (:subscriber this))
        (dissoc this :subscriber)))))

(defn geolocations-subscriber []
  (component/using
    (map->GeolocationsSubscriber {})
    [:es :hornetq-geolocations]))
