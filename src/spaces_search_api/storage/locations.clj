(ns spaces-search-api.storage.locations
  (:require [taoensso.timbre :as timbre]
            [clojurewerkz.elastisch.query :as q]
            [clojurewerkz.elastisch.native.index :as esi]
            [clojurewerkz.elastisch.native.response :as esr]
            [clojurewerkz.elastisch.native.document :as esd]))

(timbre/refer-timbre)

(defmulti query-location (fn [filter conn index m-type query] filter))

(defmethod query-location :distance-filter [_ conn index m-type query]
  (let [query (q/filtered 
                :query  {:match_all {}} 
                :filter {:geo_distance {:distance (:distance query) 
                                        "geocodes" {:lat (:lat query) 
                                                    :lon (:long query)}}})
        res (esd/search conn index m-type :query query)
        hits (esr/total-hits res)]
    {:hits hits :ids (esr/ids-from res)}))

(defmethod query-location :distance-range-filter [_ conn index m-type query]
  (let [query (q/filtered 
                :query  {:match_all {}}
                :filter {:geo_distance_range {:from (:from-distance query)
                                              :to (:to-distance query)
                                              "geocodes" {:lat (:lat query)
                                                          :lon (:long query)}}})
        res (esd/search conn index m-type :query query)
        hits (esr/total-hits res)]
    {:hits hits :ids (esr/ids-from res)}))

(defmethod query-location :polygon-filter [_ conn index m-type query]
  (let [query (q/filtered 
                :query  {:match_all {}}
                :filter {:geo_polygon {"geocodes" {:points [{:lat (:lat-1 query) :lon (:long-1 query)}
                                                            {:lat (:lat-2 query) :lon (:long-2 query)}
                                                            {:lat (:lat-3 query) :lon (:long-3 query)}]}}})
        res (esd/search conn index m-type :query query)
        hits (esr/total-hits res)]
    {:hits hits :ids (esr/ids-from res)}))

(defmethod query-location :bounding-box-filter [_ conn index m-type query]
  (let [query (q/filtered 
                :query  {:match_all {}}
                :filter {:geo_bounding_box {"geocodes" {:top_left {:lat (:lat-top query)
                                                                   :lon (:long-top query)}
                                                        :bottom_right {:lat (:lat-bottom query)
                                                                       :lon (:long-bottom query)}}}})
        res (esd/search conn index m-type :query query)
        hits (esr/total-hits res)]
    {:hits hits :ids (esr/ids-from res)}))

(defn index-location [conn index m-type location]
  @(esd/async-put conn index m-type (:id location) location))

(defn update-location [conn index m-type location location-id]
  @(esd/async-put conn index m-type location-id location))

(defn delete-location [conn index m-type location-id]
  (esd/delete conn index m-type location-id)) 

(defn refresh-index [conn index]
  (esi/refresh conn index))
