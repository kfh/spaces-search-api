(ns spaces-search-api.service.locations
  (:require [taoensso.timbre :as timbre]
            [spaces-search-api.domain.locations :as domain]  
            [spaces-search-api.storage.locations :as storage]))

(timbre/refer-timbre)

(defn query-location [conn index m-type query]
  (-> query
      (domain/validate-location-query)  
      (as-> val-query
        (storage/query-location (:filter val-query) conn index m-type val-query))))

(defn index-location [conn index m-type location]
  (->> location
       (domain/validate-location)
       (storage/index-location conn index m-type)))

(defn update-location [conn index m-type location-id location]
  (->> location
       (domain/validate-location)
       (storage/update-location conn index m-type (domain/validate-location-id location-id))))

(defn delete-location [conn index m-type location-id]
  (->> location-id
       (domain/validate-location-id)
       (storage/delete-location conn index m-type)))

(defn get-location [conn index m-type location-id]
  (->> location-id
       (domain/validate-location-id)
       (storage/get-location conn index m-type)))

(defn refresh-location [conn index]
  (storage/refresh-index conn index))
