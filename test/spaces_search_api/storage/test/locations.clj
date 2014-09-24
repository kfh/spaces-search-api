(ns spaces-search-api.storage.test.locations
  (:require [hara.common :refer [uuid]] 
            [spaces-search-api.storage.locations :as storage]  
            [spaces-search-api.system :refer [spaces-test-db]]  
            [clojure.test :refer [deftest testing is]]
            [com.stuartsierra.component :as component]
            [clojurewerkz.elastisch.native.index :as esi]
            [clojurewerkz.elastisch.native.document :as esd]))  

(deftest index-and-get-location
  (let [system (component/start (spaces-test-db))
        {:keys [es]} system
        {:keys [conn index m-type]} es]
    (try
      (testing "Index and get location"
        (let [location {:id (str (uuid))
                        :geocodes {:lat 13.734603 :lon 100.5639662}}
              index-res (storage/index-location conn index m-type location)]
          (esi/refresh conn index)
          (is (= (:id location) (:id index-res)))
          (let [indexed-location (esd/get conn index m-type (:id location))]
            (is (= (:id location) (-> indexed-location :source :id)))
            (is (= (:geocodes location) (-> indexed-location :source :geocodes))))))
      (finally
        (component/stop system)))))

(deftest index-and-query-location-with-distance-filter
  (let [system (component/start (spaces-test-db))
        {:keys [es]} system
        {:keys [conn index m-type]} es]
    (try
      (testing "Index and query for location using distance filter"
        (let [loc-1 {:id (str (uuid)) :geocodes {:lat 13.734603 :lon 100.5639662}}
              loc-2 {:id (str (uuid)) :geocodes {:lat 13.7315902 :lon 100.56822}}
              loc-3 {:id (str (uuid)) :geocodes {:lat 13.7289616 :lon 100.5765392}}
              index-location (partial storage/index-location conn index m-type)]
          (index-location loc-1) 
          (index-location loc-2) 
          (index-location loc-3) 
          (esi/refresh conn index)
          (let [query (partial storage/query-location :distance-filter conn index m-type)]
            (is (= 3 (-> (query {:distance "5km" :lat 13.7175831 :long 100.5899095}) :hits)))
            (is (= 1 (-> (query {:distance "2km" :lat 13.7175831 :long 100.5899095}) :hits)))   
            (is (= 0 (-> (query {:distance "500m" :lat 13.7175831 :long 100.5899095}) :hits))))))   
      (finally
        (component/stop system)))))

(deftest index-and-query-location-with-distance-range-filter
  (let [system (component/start (spaces-test-db))
        {:keys [es]} system
        {:keys [conn index m-type]} es]
    (try
      (testing "Index and query for location using distance range filter"
        (let [loc-1 {:id (str (uuid)) :geocodes {:lat 13.734603 :lon 100.5639662}}
              loc-2 {:id (str (uuid)) :geocodes {:lat 13.7315902 :lon 100.56822}}
              loc-3 {:id (str (uuid)) :geocodes {:lat 13.7289616 :lon 100.5765392}}
              index-location (partial storage/index-location conn index m-type)]
          (index-location loc-1) 
          (index-location loc-2) 
          (index-location loc-3) 
          (esi/refresh conn index)
          (let [query (partial storage/query-location :distance-range-filter conn index m-type)]
            (is (= 0 (-> (query {:from-distance "10km" :to-distance "50km" :lat 13.7175831 :long 100.5899095}) :hits)))
            (is (= 1 (-> (query {:from-distance "1km" :to-distance "2km" :lat 13.7175831 :long 100.5899095}) :hits)))   
            (is (= 3 (-> (query {:from-distance "400m" :to-distance "6km" :lat 13.7175831 :long 100.5899095}) :hits))))))   
      (finally
        (component/stop system)))))

(deftest index-and-query-location-with-polygon-filter
  (let [system (component/start (spaces-test-db))
        {:keys [es]} system
        {:keys [conn index m-type]} es]
    (try
      (testing "Index and query for location using polygon filter"
        (let [loc-1 {:id (str (uuid)) :geocodes {:lat 13.734603 :lon 100.5639662}}
              loc-2 {:id (str (uuid)) :geocodes {:lat 13.7315902 :lon 100.56822}}
              loc-3 {:id (str (uuid)) :geocodes {:lat 13.7289616 :lon 100.5765392}}
              index-location (partial storage/index-location conn index m-type)]
          (index-location loc-1) 
          (index-location loc-2) 
          (index-location loc-3) 
          (esi/refresh conn index)
          (let [query (partial storage/query-location :polygon-filter conn index m-type)]
            (is (= 0 (-> (query {:lat-1 13.71 :long-1 100.55 :lat-2 13.70 :long-2 100.54 :lat-3 13.69 :long-3 100.53}) :hits)))
            (is (= 1 (-> (query {:lat-1 13.73 :long-1 100.58  :lat-2 13.70 :long-2 100.57 :lat-3 14.35 :long-3 100.55}) :hits)))
            (is (= 3 (-> (query {:lat-1 13.73 :long-1 100.59 :lat-2 13.71 :long-2 100.54 :lat-3 13.76 :long-3 100.54}) :hits))))))   
      (finally
        (component/stop system)))))

(deftest index-and-query-location-with-bounding-box-filter
  (let [system (component/start (spaces-test-db))
        {:keys [es]} system
        {:keys [conn index m-type]} es]
    (try
      (testing "Index and query for location using bounding box filter"
        (let [loc-1 {:id (str (uuid)) :geocodes {:lat 13.734603 :lon 100.5639662}}
              loc-2 {:id (str (uuid)) :geocodes {:lat 13.7315902 :lon 100.56822}}
              loc-3 {:id (str (uuid)) :geocodes {:lat 13.7289616 :lon 100.5765392}}
              index-location (partial storage/index-location conn index m-type)]
          (index-location loc-1) 
          (index-location loc-2) 
          (index-location loc-3) 
          (esi/refresh conn index)
          (let [query (partial storage/query-location :bounding-box-filter conn index m-type)]
            (is (= 1 (-> (query {:lat-top 13.7402 :long-top 100.5709 :lat-bottom 13.72 :long-bottom 100.5600}) :hits)))
            (is (= 3 (-> (query {:lat-top 13.7390 :long-top 100.5540 :lat-bottom 13.7130 :long-bottom 100.5858}) :hits)))
            (is (= 0 (-> (query {:lat-top 13.7272 :long-top 100.5680 :lat-bottom 13.7290 :long-bottom 100.5640}) :hits)))
            )))   
      (finally
        (component/stop system)))))


