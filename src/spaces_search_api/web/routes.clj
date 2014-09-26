(ns spaces-search-api.web.routes
  (:require [cheshire.core :as json] 
            [compojure.core :refer :all]
            [compojure.route :as route]
            [taoensso.timbre :as timbre]
            [liberator.core :refer [resource]]
            [spaces-search-api.service.locations :as service]   
            [com.stuartsierra.component :as component]))

(timbre/refer-timbre)

(defn- query-location [db ctx]
  (let [{:keys [conn index m-type]} db
        body (-> (slurp (-> ctx :request :body)))
        body->edn (json/parse-string body true)]
    (service/query-location conn index m-type body->edn)))

(defn- index-location [db ctx]
  (let [{:keys [conn index m-type]} db] 
    (service/index-location conn index m-type (:params ctx))))

(defn- update-location [db ctx location-id]
  (let [{:keys [conn index m-type]} db] 
    (service/update-location conn index m-type (:params ctx) location-id)))

(defn- delete-location [db location-id]
  (let [{:keys [conn index m-type]} db]
    (service/delete-location conn index m-type location-id)))

(defn- get-location [db location-id]
  (let [{:keys [conn index m-type]} db] 
    (service/get-location conn index m-type location-id)))

(defn- refresh-location [db]
  (let [{:keys [conn index]} db] 
    (service/refresh-location conn index)))

(defrecord ApiRoutes [es]
  component/Lifecycle

  (start [this]
    (info "Enabling api routes")
    (if (:routes this)
      this 
      (let [api-routes 
            (context "/api" []
                     (ANY "/locations/refresh" [] 
                          (resource
                            :allowed-methods [:get]
                            :available-media-types ["application/json"] 
                            :handle-ok (fn [_] (refresh-location es))))
                     (ANY "/locations/query" [] 
                          (resource 
                            :allowed-methods [:get]
                            :available-media-types ["application/json"] 
                            :handle-ok (fn [ctx] (query-location es ctx))))
                     (ANY "/locations" []
                          (resource 
                            :allowed-methods [:post]
                            :available-media-types ["application/json"]  
                            :post! (fn [ctx] {::res (index-location es (:request ctx))})
                            :handle-created ::res))
                     (ANY "/locations/:loc-id" [loc-id] 
                          (resource 
                            :allowed-methods [:get :put :delete]
                            :available-media-types ["application/json"] 
                            :exists? (fn [_] (when-let [loc (get-location es loc-id)] {::res loc}))
                            :handle-ok ::res   
                            :put! (fn [ctx] {::res (update-location es (:request ctx) loc-id)})   
                            :handle-created ::res
                            :delete! (fn [_] (delete-location es loc-id)))))]
        (assoc this :routes api-routes))))

  (stop [this]
    (info "Disabling api routes")
    (if-not (:routes this) 
      this
      (dissoc this :routes))))

(defn api-routes []
  (component/using
    (map->ApiRoutes {})
    [:es]))
