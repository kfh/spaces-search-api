(ns spaces-search-api.service.locations
  (:require [taoensso.timbre :as timbre]
            [spaces-search-api.storage.locations :as storage]))

(timbre/refer-timbre)

(defn query-location [conn index m-type query]
  (storage/query-location (-> query :filter keyword) conn index m-type query))

(defn index-location [conn index m-type params]
  (storage/index-location conn index m-type params))

(defn update-location [conn index m-type params location-id]
  (storage/update-location conn index m-type params location-id))

(defn delete-location [conn index m-type location-id]
  (storage/delete-location conn index m-type location-id))

(defn refresh-location [conn index]
  (storage/refresh-index conn index))
