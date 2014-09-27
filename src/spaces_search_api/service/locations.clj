(ns spaces-search-api.service.locations
  (:require [taoensso.timbre :as timbre]
            [spaces-search-api.domain.locations :as domain]  
            [spaces-search-api.storage.locations :as storage]))

(timbre/refer-timbre)

(defn query-location [conn index m-type query]
  (-> query
      (domain/coerce-location-query)
      (domain/validate-location-query)  
      (as-> val-query
        (storage/query-location (:filter val-query) conn index m-type val-query))))

(defn index-location [conn index m-type params]
  (storage/index-location conn index m-type params))

(defn update-location [conn index m-type params location-id]
  (storage/update-location conn index m-type params location-id))

(defn delete-location [conn index m-type location-id]
  (storage/delete-location conn index m-type location-id))

(defn get-location [conn index m-type location-id]
  (storage/get-location conn index m-type location-id))

(defn refresh-location [conn index]
  (storage/refresh-index conn index))
