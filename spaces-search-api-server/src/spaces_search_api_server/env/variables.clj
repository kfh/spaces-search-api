(ns spaces-search-api-server.env.variables
  (:require [environ.core :refer [env]]
            [ribol.core :refer [raise]]
            [taoensso.timbre :as timbre]
            [com.stuartsierra.component :as component]))

(timbre/refer-timbre)

(defn- assemble-es-host [variables] 
  (if-let [es-host (:elasticsearch-host env)]
    (assoc variables :es-host es-host)
    (raise {:error "Env variable ELASTICSEARCH_HOST not set!"})))

(defn- assemble-es-port [variables] 
  (if-let [es-port (:elasticsearch-port env)]
     (assoc variables :es-port (read-string es-port))
    (raise {:error "Env variable ELASTICSEARCH_PORT not set!"})))

(defn- assemble-es-cluster-name[variables] 
  (if-let [es-cluster-name (:elasticsearch-cluster-name env)]
    (assoc variables :es-cluster-name es-cluster-name)
    (do 
      (warn "ELASTICSEARCH_CLUSTER_NAME not set, defaulting to 'elasticsearch'")
      (assoc variables :es-cluster-name "elasticsearch"))))

(defrecord Environment []
  component/Lifecycle 
  
  (start [this]
    (info "Assembling Environment")
    (cond->
      this
      ((complement :es-host) this) (assemble-es-host)
      ((complement :es-port) this) (assemble-es-port)
      ((complement :es-cluster-name) this) (assemble-es-cluster-name)))
  
  (stop [this]
    (info "Disassembling Environment")
    (cond-> 
      this
      (:es-host this) (dissoc :es-host)
      (:es-port this) (dissoc :es-port)
      (:es-cluster-name this) (dissoc :es-cluster-name))))

(defn environment []
  (component/using 
    (map->Environment {})
     [:logger]))

(defn environment-without-logger []
  (map->Environment {}))
