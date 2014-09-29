(ns spaces-search-api.web.routes
  (:require [cheshire.core :as json] 
            [compojure.core :refer :all]
            [compojure.route :as route]
            [taoensso.timbre :as timbre]
            [liberator.core :refer [defresource]]
            [spaces-search-api.service.locations :as service]   
            [com.stuartsierra.component :as component]))

(timbre/refer-timbre)

(defn- query-location [db ctx]
  (let [{:keys [conn index m-type]} db
        body (-> ctx :request :body slurp)
        query (json/parse-string body true)]
    (service/query-location conn index m-type query)))

(defn- index-location [db ctx]
  (let [{:keys [conn index m-type]} db] 
    (service/index-location conn index m-type (:params ctx))))

(defn- update-location [db location-id req]
  (let [{:keys [conn index m-type]} db] 
    (service/update-location conn index m-type location-id (:params req))))

(defn- delete-location [db location-id]
  (let [{:keys [conn index m-type]} db]
    (service/delete-location conn index m-type location-id)))

(defn- get-location [db location-id]
  (let [{:keys [conn index m-type]} db] 
    (service/get-location conn index m-type location-id)))

(defn- refresh-location [db]
  (let [{:keys [conn index]} db] 
    (service/refresh-location conn index)))

(defresource refresh-resource [es]
  :allowed-methods [:get]
  :available-media-types ["application/json"] 
  :handle-ok (fn [_] (refresh-location es)))

(defresource query-resource [es]
  :allowed-methods [:get]
  :available-media-types ["application/json"] 
  :handle-ok (fn [ctx] (query-location es ctx))
  :handle-exception (fn [ctx] {::error (.getMessage (:exception ctx))}))

(defresource create-resource [es] 
  :allowed-methods [:post]
  :available-media-types ["application/json"]  
  :post! (fn [ctx] {::res (index-location es (:request ctx))})
  :handle-created ::res
  :handle-exception (fn [ctx] {::error (.getMessage (:exception ctx))}))

(defresource location-resource [es id]
  :allowed-methods [:get :put :delete]
  :available-media-types ["application/json"] 
  :exists? (fn [_] (when-let [loc (get-location es id)] {::res loc}))
  :handle-ok ::res   
  :put! (fn [ctx] {::res (update-location es id (:request ctx))})   
  :handle-created ::res
  :delete! (fn [_] (delete-location es id))
  :handle-exception (fn [ctx] {::error (.getMessage (:exception ctx))}))

(defrecord ApiRoutes [es]
  component/Lifecycle

  (start [this]
    (info "Enabling api routes")
    (if (:routes this)
      this 
      (->> (context "/api" []
                    (ANY "/locations/refresh" [] (refresh-resource es))
                    (ANY "/locations/query" [] (query-resource es))
                    (ANY "/locations" [] (create-resource es))  
                    (ANY "/locations/:id" [id] (location-resource es id)))
           (assoc this :routes))))

  (stop [this]
    (info "Disabling api routes")
    (if-not (:routes this) 
      this
      (dissoc this :routes))))

(defn api-routes []
  (component/using
    (map->ApiRoutes {})
    [:es]))
