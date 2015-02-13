(ns spaces-search-api-server.storage.db
  (:require [hara.common :refer [uuid]]
            [taoensso.timbre :as timbre]
            [clojurewerkz.elastisch.native :as es] 
            [com.stuartsierra.component :as component]  
            [rubberlike.core :refer [create stop client]]  
            [clojurewerkz.elastisch.native.index :as esi]
            [clojurewerkz.elastisch.native.response :as esrsp]))

(timbre/refer-timbre)

(defrecord Elasticsearch [env]
  component/Lifecycle

  (start [this]
    (info "Starting Elasticsearch")
    (if (:conn this) 
      this
      (let [conn (es/connect [[(:es-host env) (:es-port env)]]
                             {"cluster.name" (:es-cluster-name env)}) 
            index "spaces_development"
            m-type "location"
            mapping-types {"location" {:properties {:geocodes {:type "geo_point"}}}}]
        (when-not (esi/exists? conn index)
          (info "Creating index: " index)
          (-> (esi/create conn index :mappings mapping-types)
              (esrsp/acknowledged?)
              (assert)))
        (assoc this :conn conn :index index :m-type m-type))))

  (stop [this]
    (info "Stopping Elasticsearch")
    (if-not (:conn this) 
      this
      (let [{:keys [conn index]} this] 
        (dissoc this :conn :index :m-type)))))

(defrecord ElasticsearchTest []
  component/Lifecycle

  (start [this]
    (info "Starting Elasticsearch")
    (if (:es this) 
      this
      (let [es (create {:disable-http? true})
            conn (client es) 
            index "spaces_test"
            m-type "location"
            mapping-types {"location" {:properties {:geocodes {:type "geo_point"}}}}]
        (info "Creating index: " index)
        (-> (esi/create conn index :mappings mapping-types)
            (esrsp/acknowledged?)
            (assert))
        (assoc this :es es :conn conn :index index :m-type m-type))))

  (stop [this]
    (info "Stopping Elasticsearch")
    (if-not (:es this) 
      this
      (let [{:keys [es conn index]} this] 
        (info "Deleting index: " index)
        (esi/delete conn index)
        (stop es)
        (dissoc this :es :conn :index :m-type)))))

(defn elasticsearch []
  (component/using 
    (map->Elasticsearch {})
    [:env]))

(defn elasticsearch-test []
  (map->ElasticsearchTest {}))
