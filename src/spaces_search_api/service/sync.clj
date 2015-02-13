(ns spaces-search-api.service.sync
  (:require [taoensso.timbre :as timbre]
            [com.stuartsierra.component :as component]  
            [clojure.core.async :refer [chan close!]]))

(timbre/refer-timbre)

(defrecord LocationSync []
  component/Lifecycle
  
  (start [this]
    (info "Starting location sync")
    (if (:sync this)
      this))
  
  (stop [this]
    (info "Stopping location sync")
    (if-not (:sync this)
      this)))

(defn location-sync []
  (map->LocationSync {}))
