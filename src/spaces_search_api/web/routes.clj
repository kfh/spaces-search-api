(ns spaces-search-api.web.routes
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [taoensso.timbre :as timbre]
            [spaces-search-api.service.locations :as service]   
            [com.stuartsierra.component :as component]))

(timbre/refer-timbre)

(defn- query-location [db query]
  (let [{:keys [conn index m-type]} db] 
    (if-let [locations (service/query-location conn index m-type query)]
      {:status 200 :body locations}
      {:status 404})))

(defn- index-location [db req]
  (let [{:keys [conn index m-type]} db] 
    {:status 201
     :body (service/index-location conn index m-type (:params req))}))

(defn- update-location [db req location-id]
  (let [{:keys [conn index m-type]} db] 
    {:status 201
     :body (service/update-location conn index m-type (:params req) location-id)}))

(defn- delete-location [db location-id]
  (let [{:keys [conn index m-type]} db] 
    (if (service/delete-location conn index m-type location-id)
      {:status 204}
      {:status 404})))

(defrecord ApiRoutes [es]
  component/Lifecycle

  (start [this]
    (info "Enabling api routes")
    (if (:routes this)
      this 
      (let [api-routes (routes
                         (GET "/location/:query" [query :as req] (query-location es query))
                         (POST "/location" req (index-location es req))
                         (PUT "/location/:loc-id" [loc-id :as req] (update-location es req loc-id))
                         (DELETE "/location/:loc-id" [loc-id :as req] (delete-location loc-id))
                         (route/resources "/")
                         (route/not-found "Not Found"))]
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
