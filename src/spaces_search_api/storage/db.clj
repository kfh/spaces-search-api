(ns spaces-search-api.storage.db
  (:require [hara.common :refer [uuid]]
            [cheshire.core :as json]
            [clj-http.client :as http] 
            [clj-http.conn-mgr :as mgr] 
            [taoensso.timbre :as timbre]
            [clojurewerkz.elastisch.native :as es] 
            [clojurewerkz.elastisch.native.index :as esi]
            [com.stuartsierra.component :as component]))

(timbre/refer-timbre)

(defn- ->cluster-name [host port]
  (-> (http/get (str "http://" host ":" port "/_nodes/cluster"))
      :body
      (json/parse-string true)
      :cluster_name))

(defrecord Elasticsearch [host ports]
  component/Lifecycle

  (start [this]
    (info "Starting Elasticsearch")
    (if (:conn this) 
      this
      (let [conn (es/connect [[host (:native ports)]]
                             {"cluster.name" (->cluster-name host (:web ports))}) 
            index "spaces_development"
            m-type "location"
            mapping-types {"location" {:properties {:geocodes {:type "geo_point"}}}}]
        (when-not (esi/exists? conn index)
          (info "Creating index: " index)
          (-> (esi/create conn index :mappings mapping-types)
              :ok
              (assert)))
        (assoc this :conn conn :index index :m-type m-type))))

  (stop [this]
    (info "Stopping Elasticsearch")
    (if-not (:conn this) 
      this
      (let [{:keys [conn index]} this] 
        (do
          (when (esi/exists? conn index)
            (info "Deleting index: " index)
            (esi/delete conn index))
          (dissoc this :conn :index :m-type))))))

(defn es [host ports]
  (map->Elasticsearch {:host host :ports ports}))

(defn es-test []
  (map->Elasticsearch {:host "127.0.0.1" :ports {:web 9200 :native 9300}}))
