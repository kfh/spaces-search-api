(ns spaces-search-api.storage.db
  (:require [hara.common :refer [uuid]]
            [taoensso.timbre :as timbre]
            [clojurewerkz.elastisch.native :as es] 
            [clojurewerkz.elastisch.native.index :as esi]
            [clojurewerkz.elastisch.native.response :as res] 
            [com.stuartsierra.component :as component]))

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
              (res/acknowledged?)
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

(defn elasticsearch []
  (component/using 
    (map->Elasticsearch {})
    [:env]))
