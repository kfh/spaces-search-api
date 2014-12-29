(ns spaces-search-api.service.locations
  (:require [taoensso.timbre :as timbre]
            [spaces-search-api.domain.locations :as domain]  
            [spaces-search-api.storage.locations :as storage]))

(timbre/refer-timbre)

(defn query-location [conn index m-type query]
  (-> query
      (domain/validate-location-query)  
      (as-> validated-query
        (let [[query error] validated-query]  
          (if error
            error
            (storage/query-location (:filter query) conn index m-type query))))))

(defn index-location [conn index m-type location]
  (-> location
      (domain/validate-location)
      (as-> validated-location
        (let [[location error] validated-location]
          (if error
            error
            (storage/index-location conn index m-type location))))))

(defn update-location [conn index m-type location-id location]
  (let [[location-id error] (domain/validate-location-id location-id)] 
    (if error
      error
      (-> location
          (domain/validate-location)
          (as-> validated-location
            (let [[location error] validated-location]
              (if error
                error
                (storage/update-location conn index m-type location-id location))))))))

(defn delete-location [conn index m-type location-id]
  (-> location-id
      (domain/validate-location-id)
      (as-> validated-location-id
        (let [[location-id error] validated-location-id]
          (if error
            error
            (storage/delete-location conn index m-type location-id))))))

(defn get-location [conn index m-type location-id]
  (-> location-id
      (domain/validate-location-id)
      (as-> validated-location-id 
        (let [[location-id error] validated-location-id]
          (if error
            error
            (storage/get-location conn index m-type location-id))))))

(defn refresh-location [conn index]
  (storage/refresh-index conn index))
