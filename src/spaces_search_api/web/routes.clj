(ns spaces-search-api.web.routes
  (:require [cheshire.core :as json] 
            [taoensso.timbre :as timbre]
            [cognitect.transit :as transit]
            [compojure.core :refer [ANY context]]
            [liberator.core :refer [defresource]]
            [io.clojure.liberator-transit :refer :all] 
            [com.stuartsierra.component :as component]
            [spaces-search-api.domain.locations :as domain]   
            [spaces-search-api.service.locations :as service]))     

(timbre/refer-timbre)

(defn- query-location [db query]
  (let [{:keys [conn index m-type]} db]
    (service/query-location conn index m-type query)))

(defn- index-location [db location]
  (let [{:keys [conn index m-type]} db] 
    (service/index-location conn index m-type location)))

(defn- update-location [db location-id location]
  (let [{:keys [conn index m-type]} db] 
    (service/update-location conn index m-type location-id location)))

(defn- delete-location [db location-id]
  (let [{:keys [conn index m-type]} db]
    (service/delete-location conn index m-type location-id)))

(defn- get-location [db location-id]
  (let [{:keys [conn index m-type]} db] 
    (service/get-location conn index m-type location-id)))

(defn- parse-transit-json [ctx key]
  (if-let [body (-> ctx :request :body)]
    (try 
      (-> body (transit/reader :json) (transit/read)
          (as-> data [false {key data}]))
      (catch Exception e
        {::error (.getMessage e)}))
    {::error "No body found"}))

(defn- validate [f data key]
  (let [[data error] (f data)] 
    (if-not error
      [true {key data}]
      [false {key {::error (select-keys error [:value :type])}}])))

(defn- validate-query [query key]
  (validate domain/validate-location-query query key))

(defn- validate-location [ctx key]
  (if (#{:put :post} (-> ctx :request :request-method)) 
    (let [location (-> ctx :request :params)] 
      (validate domain/validate-location location key))
    true))

(defresource query-resource [es]
  :allowed-methods [:get]
  :available-media-types ["application/transit+json"] 
  :as-response (as-response {:allow-json-verbose? false}) 
  :malformed? (fn [ctx] (parse-transit-json ctx ::json))
  :processable? (fn [ctx] (validate-query (::json ctx) ::query))
  :handle-ok (fn [ctx] (query-location es (::query ctx)))
  :handle-unprocessable-entity ::query
  :handle-exception (fn [ctx] {::error (.getMessage (:exception ctx))}))

(defresource create-resource [es] 
  :allowed-methods [:post]
  :available-media-types ["application/transit+json"]  
  :as-response (as-response {:allow-json-verbose? false}) 
  :processable? (fn [ctx] (validate-location ctx ::location))
  :post! (fn [ctx] {::res (index-location es (::location ctx))})
  :handle-created ::res
  :handle-unprocessable-entity ::location
  :handle-exception (fn [ctx] {::error (.getMessage (:exception ctx))}))

(defresource location-resource [es id]
  :allowed-methods [:get :put :delete]
  :available-media-types ["application/transit+json"] 
  :as-response (as-response {:allow-json-verbose? false}) 
  :processable? (fn [ctx] (validate-location ctx ::location))
  :exists? (fn [_] (when-let [loc (get-location es id)] {::res loc}))
  :handle-ok ::res   
  :put! (fn [ctx] {::res (update-location es id (::location ctx))})   
  :handle-created ::res
  :delete! (fn [_] (delete-location es id))
  :handle-unprocessable-entity ::location
  :handle-exception (fn [ctx] {::error (.getMessage (:exception ctx))}))

(defrecord ApiRoutes [es]
  component/Lifecycle

  (start [this]
    (info "Enabling api routes")
    (if (:routes this)
      this 
      (->> (context "/api" []
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
