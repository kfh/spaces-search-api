(ns spaces-search-api-server.service.subscriber
  (:require [taoensso.timbre :as timbre]
            [cognitect.transit :as transit]
            [com.stuartsierra.component :as component]
            [clojure.core.async :refer [<! close! go-loop]]
            [spaces-search-api-server.service.locations :as service])
  (import [java.io ByteArrayInputStream]))

(timbre/refer-timbre)

(defn- read-transit [geolocation]
  (-> geolocation
      (ByteArrayInputStream.)
      (transit/reader :json)
      (transit/read)))

(defn- take-and-index-geolocations [es subscriber]
  (let [{:keys [conn index m-type]} es] 
    (go-loop []
      (when-let [geolocations (<! subscriber)]
        (->> geolocations
             (read-transit) 
             (spy :info "Preparing to index: ") 
             (service/index-locations conn index m-type)
             (spy :info "Index result: ")))
      (recur))))

(defrecord ZeroMQSubscriber [es queue]
  component/Lifecycle

  (start [this]
    (info "Starting ZeroMQ subscriber")
    (if (:subscriber this)
      this
      (->> queue 
           :sub-channel
           (take-and-index-geolocations es)
           (assoc this :subscriber))))

  (stop [this]
    (info "Stopping ZeroMQ subscriber")
    (if-not (:subscriber this)
      this
      (do
        (close! (:subscriber this))
        (dissoc this :subscriber)))))

(defn zeromq-subscriber []
  (component/using
    (map->ZeroMQSubscriber {})
    [:es :queue]))



