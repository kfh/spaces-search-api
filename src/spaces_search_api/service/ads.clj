(ns spaces-search-api.service.ads
  (:require [taoensso.timbre :as timbre]
            [spaces-search-api.storage.ads :as storage]))

(timbre/refer-timbre)

(defn query-ads [conn index query]
  (storage/query-ads conn index query))

(defn index-ad [conn index params]
  (storage/index-ad conn index params (:id params)))

(defn update-ad [conn index params ad-id]
  (storage/update-ad conn index params ad-id))

(defn delete-ad [conn index ad-id]
  (storage/delete-ad conn index ad-id))
