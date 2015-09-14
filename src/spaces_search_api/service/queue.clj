(ns spaces-search-api.service.queue
  (:require [taoensso.timbre :as timbre]
            [cognitect.transit :as transit]
            [com.stuartsierra.component :as component]
            [clojure.core.async :refer [put! chan close!]]   
            [immutant.messaging :refer [topic context stop subscribe]])
  (import [java.io ByteArrayInputStream]))

(timbre/refer-timbre)

(defn- read-transit [geolocation]
  (-> geolocation
      (ByteArrayInputStream.)
      (transit/reader :json)
      (transit/read)))

(defrecord HornetQGeolocations []
  component/Lifecycle

  (start [this]
    (info "Starting HornetQ(geolocations)")
    (if (:sub-out this)
      this
      (let [sub-out (chan)
            f #(put! sub-out (read-transit %))
            ctx (context :host "localhost" :port 5445 :client-id "55f2c999")  
            t (topic "geolocations" :context ctx)
            listener (subscribe t "geolocations-listener" f)]
        (assoc this :listener listener :ctx ctx :sub-out sub-out))))

  (stop [this]
    (info "Stopping HornetQ(geolocations)")
    (if-not (:sub-out this)
      this
      (do
        (stop (:listener this))
        (stop (:ctx this))
        (close! (:sub-out this))
        (dissoc this :listener :ctx :sub-out)))))

(defn hornetq-geolocations []
  (map->HornetQGeolocations {}))
