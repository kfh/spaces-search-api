(ns spaces-search-api-server.service.queue
  (:require [taoensso.timbre :as timbre]
            [com.stuartsierra.component :as component]  
            [clojure.core.async :refer [chan close!]]   
            [com.keminglabs.zmq-async.core :refer [register-socket!]]))

(timbre/refer-timbre)

(def publisher "tcp://*:17778")

(def sub-in (chan 1024))
(def sub-out (chan 1024))

(def sub {:in sub-in
          :out sub-out
          :socket-type :sub
          :configurator (fn [socket]
                          (do
                            (.connect socket publisher)
                            (.subscribe socket (byte-array 0))))})

(defrecord ZeroMQ []
  component/Lifecycle
  
  (start [this]
    (info "Starting ZeroMQ")
    (if (:sub-channel this)
      this
      (do
        (register-socket! sub)
        (assoc this :sub-channel sub-out))))
  
  (stop [this]
    (info "Stopping ZeroMQ")
    (if-not (:sub-channel this)
      this
      (do
        (close! sub-in)
        (dissoc this :sub-channel)))))

(defn zeromq []
  (map->ZeroMQ {}))
