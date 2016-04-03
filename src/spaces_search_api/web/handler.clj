(ns spaces-search-api.web.handler
  (:require [taoensso.timbre :as timbre]
            [compojure.api.sweet :refer :all]
            [ring.util.http-response :refer :all]
            [ring.swagger.schema :refer [coerce!]]
            [com.stuartsierra.component :as component]
            [spaces-search-api.domain.locations :refer :all]
            [spaces-search-api.service.locations :as service])
  (:import (java.util UUID)))

(timbre/refer-timbre)

(defrecord RingHandler [es]
  component/Lifecycle

  (start [this]
    (info "Enabling ring handler")
    (if (:handler this)
      this
      (->> (api
             (ring.swagger.ui/swagger-ui
               "/swagger-ui")
             (swagger-docs
               {:info {:title "Spaces Search API"}})
             {:formats [:transit-json :edn]}
             ;{:params-opts {:transit-json {:options {:handlers :readers}}}}
             {:response-opts {:transit-json {:options {:handlers :writers}}}}
             (context* "/api" []
               :tags ["locations"]

               (GET* "/locations/query" []
                 :return [Location]
                 :body [query :- FilterQuery]
                 :summary "returns indexed locations matching the query."
                 (ok (service/query-location (:conn es) (:index es) (:m-type es) query)))

               (GET* "/locations/:id" []
                 :return Location
                 :path-params [id :- UUID]
                 :summary "returns the location with given id."
                 (if-let [location (service/get-location (:conn es) (:index es) (:m-type es) id)] (ok location) (not-found)))

               (POST* "/locations" []
                 :return Location
                 :body [location NewLocation]
                 :summary "index a new location."
                 (ok (service/index-location (:conn es) (:index es) (:m-type es) location)))

               (PUT* "/locations" []
                 :return Location
                 :body [location Location]
                 :summary "updates an indexed location."
                 (ok (service/update-location (:conn es) (:index es) (:m-type es) location)))

               (DELETE* "/locations/:id" []
                 :path-params [id :- UUID]
                 :summary "deletes the indexed location with given id."
                 (if (service/delete-location (:conn es) (:index es) (:m-type es) id) (no-content) (not-found)))))
        (assoc this :handler))))

  (stop [this]
    (info "Disabling ring handler")
    (if-not (:handler this)
      this
      (dissoc this :handler))))

(defn ring-handler []
  (component/using 
    (map->RingHandler {})
    [:es]))
