(ns spaces-search-api.service.locations
  (:require [taoensso.timbre :as timbre]
            [spaces-search-api.storage.locations :as storage]))

(timbre/refer-timbre)

(defn query-location [conn index m-type query]
  (storage/query-location (:filter query) conn index m-type query))

(defn index-location [conn index m-type location]
  (storage/index-location conn index m-type location))

(defn index-locations [conn index m-type locations]
  (for [location locations]
    (-> location
      (dissoc :added)
      (index-location conn index m-type))))

(defn update-location [conn index m-type location]
  (storage/update-location conn index m-type location))

(defn delete-location [conn index m-type location-id]
  (storage/delete-location conn index m-type location-id))

(defn get-location [conn index m-type location-id]
  (storage/get-location conn index m-type location-id))
