(ns spaces-search-api.storage.ads
  (:require [taoensso.timbre :as timbre]
            [clojurewerkz.elastisch.query :as q]
            [clojurewerkz.elastisch.native.response :as esr]
            [clojurewerkz.elastisch.native.document :as esd]))

(timbre/refer-timbre)

(defn query-ads-by-query-string [conn index query]
  (let [query (q/query-string 
                 :query query 
                 :allow_leading_wildcard false :default_operator "AND") 
        res (esd/search conn index "ad" :query query)
        hits (esr/total-hits res)]
    {:hits hits :ids (esr/ids-from res)}))

(defn query-ads-by-geo-distance [conn index query]
  (let [query (q/filtered 
                :query  {:match_all {}} 
                :filter {:geo_distance 
                         {:distance (:distance query)
                          "pin.location" {:lat (:lat query)
                                          :lon (:long query)}}})
        res (esd/search conn index "ad" :query query)
        hits (esr/total-hits res)]
    {:hits hits :ids (esr/ids-from res)}))'

(defn index-ad [conn index ad]
  @(esd/async-put conn index "ad" (:ad-id ad) ad))

(defn update-ad [conn index ad ad-id]
  (esd/put conn index "ad" ad-id ad)) 

(defn delete-ad [conn index ad-id]
  (esd/delete conn index "ad" ad-id)) 

