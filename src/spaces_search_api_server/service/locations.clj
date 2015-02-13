(ns spaces-search-api-server.service.locations
  (:require [taoensso.timbre :as timbre]
            [spaces-search-api-server.domain.locations :as domain]  
            [spaces-search-api-server.storage.locations :as storage]))

(timbre/refer-timbre)

(defn query-location [conn index m-type query]
  (-> query
      (domain/validate-location-query)  
      (as-> validated-query
        (let [[query error] validated-query]  
          (if error
            error
            (storage/query-location (:filter query) conn index m-type query))))))

(defn query-freshest-location [conn index m-type query]
  (storage/query-freshest-location conn index m-type query))

(defn index-location [conn index m-type location]
  (-> location
      (domain/validate-location)
      (as-> validated-location
        (let [[location error] validated-location]
          (if error
            error
            (storage/index-location conn index m-type location))))))

(defn index-locations [conn index m-type locations]
  (for [location locations] 
    (-> location
        (dissoc :added) 
        (as-> parsed-location
          (index-location conn index m-type parsed-location)))))

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
