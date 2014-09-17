(ns spaces-search-api.storage.test.ads
  (:require [hara.common :refer [uuid]] 
            [spaces-search-api.storage.ads :as ads]  
            [spaces-search-api.system :refer [spaces-test-db]]  
            [clojure.test :refer [deftest testing is]]
            [com.stuartsierra.component :as component]
            [clojurewerkz.elastisch.native.index :as esi]
            [clojurewerkz.elastisch.native.document :as esd]))  

(deftest index-and-get-ad
  (let [system (component/start (spaces-test-db))
        {:keys [es]} system
        {:keys [conn index]} es]
    (try
      (testing "Index and query for ads"
        (let [new-ad {:ad-id (str (uuid))
                      :ad-type "ad.type/real-estate" 
                      :ad-start-time "14:45" 
                      :ad-end-time "20:00" 
                      :ad-active true
                      :res-title "New apartment in central Sukhumvit"
                      :res-desc "Beatiful apartment with perfect location.."
                      :res-type "real-estate.type/apartment"
                      :res-cost "100 000 "
                      :res-size "95 m2"
                      :res-bedrooms "3"
                      :res-features ["real-estate.feature/elevator" "real-estate.feature/aircondition"]
                      :loc-name "Sukhumvit Road"
                      :loc-street "Sukhumvit Road"
                      :loc-street-num "413"
                      :loc-zip-code "10110"
                      :loc-city "Bangkok" 
                      :geo-lat 13.734603
                      :geo-long 100.5639662}
              index-res (ads/index-ad conn index new-ad)]
          (esi/refresh conn index)
          (is (= (:ad-id new-ad) (:id index-res)))
          (let [indexed-ad (esd/get conn index "ad" (:ad-id new-ad))]
            (is (= (:ad-id new-ad) (-> indexed-ad :source :ad-id)))
            (is (= (:ad-type new-ad) (-> indexed-ad :source :ad-type)))   
            (is (= (:ad-start-time new-ad) (-> indexed-ad :source :ad-start-time)))   
            (is (= (:ad-end-time new-ad) (-> indexed-ad :source :ad-end-time)))   
            (is (= (:ad-active new-ad) (-> indexed-ad :source :ad-active)))   
            (is (= (:res-title new-ad) (-> indexed-ad :source :res-title)))   
            (is (= (:res-desc new-ad) (-> indexed-ad :source :res-desc)))   
            (is (= (:res-type new-ad) (-> indexed-ad :source :res-type)))   
            (is (= (:res-cost new-ad) (-> indexed-ad :source :res-cost)))   
            (is (= (:res-size new-ad) (-> indexed-ad :source :res-size)))   
            (is (= (:res-bedrooms new-ad) (-> indexed-ad :source :res-bedrooms)))   
            (is (= (:res-features new-ad) (-> indexed-ad :source :res-features)))   
            (is (= (:loc-name new-ad) (-> indexed-ad :source :loc-name)))   
            (is (= (:loc-street new-ad) (-> indexed-ad :source :loc-street)))   
            (is (= (:loc-street-num new-ad) (-> indexed-ad :source :loc-street-num)))   
            (is (= (:loc-zip-code new-ad) (-> indexed-ad :source :loc-zip-code)))   
            (is (= (:loc-city new-ad) (-> indexed-ad :source :loc-city)))   
            (is (= (:geo-lat new-ad) (-> indexed-ad :source :geo-lat)))      
            (is (= (:geo-long new-ad) (-> indexed-ad :source :geo-long))))))
      (finally
        (component/stop system)))))

(deftest index-and-query-for-ad-by-query-string
  (let [system (component/start (spaces-test-db))
        {:keys [es]} system
        {:keys [conn index]} es]
    (try
      (testing "Index and query for ads by query string"
        (let [new-ad {:ad-id (str (uuid))
                      :ad-type "ad.type/real-estate" 
                      :ad-start-time "14:45" 
                      :ad-end-time "20:00" 
                      :ad-active true
                      :res-title "New apartment in central Sukhumvit"
                      :res-desc "Beatiful apartment with perfect location.."
                      :res-type "real-estate.type/apartment"
                      :res-cost "100 000 "
                      :res-size "95 m2"
                      :res-bedrooms "3"
                      :res-features ["real-estate.feature/elevator" "real-estate.feature/aircondition"]
                      :loc-name "Sukhumvit Road"
                      :loc-street "Sukhumvit Road"
                      :loc-street-num "413"
                      :loc-zip-code "10110"
                      :loc-city "Bangkok" 
                      :geo-lat 13.734603
                      :geo-long 100.5639662}
              index-res (ads/index-ad conn index new-ad)]
          (esi/refresh conn index)
          (let [query (partial ads/query-ads-by-query-string conn index)]
            (is (= (:id index-res) (-> (query "sukhumvit") :ids first)))
            (is (= (:id index-res) (-> (query "elevator") :ids first)))   
            (is (= (:id index-res) (-> (query "10110") :ids first)))   
            (is (= (:id index-res) (-> (query "real-estate") :ids first)))   
            (is (= (:id index-res) (-> (query "13.734603") :ids first)))   
            (is (= (:id index-res) (-> (query "perfect") :ids first)))
            (is (= #{} (:ids (query "perf"))))
            (is (= #{} (:ids (query "dreadful"))))
            (is (= #{} (:ids (query "Phuket")))))))   
      (finally
        (component/stop system)))))

(deftest index-and-query-for-ads-by-geo-distance
  (let [system (component/start (spaces-test-db))
        {:keys [es]} system
        {:keys [conn index]} es]
    (try
      (testing "Index and query for ads by geo distance"
        (let [new-ad {:ad-id (str (uuid))
                      :ad-type "ad.type/real-estate" 
                      :ad-start-time "14:45" 
                      :ad-end-time "20:00" 
                      :ad-active true
                      :res-title "New apartment in central Sukhumvit"
                      :res-desc "Beatiful apartment with perfect location.."
                      :res-type "real-estate.type/apartment"
                      :res-cost "100 000 "
                      :res-size "95 m2"
                      :res-bedrooms "3"
                      :res-features ["real-estate.feature/elevator" "real-estate.feature/aircondition"]
                      :loc-name "Sukhumvit Road"
                      :loc-street "Sukhumvit Road"
                      :loc-street-num "413"
                      :loc-zip-code "10110"
                      :loc-city "Bangkok" 
                      :geo-lat 13.734603
                      :geo-long 100.5639662}
              index-res (ads/index-ad conn index new-ad)]
          (esi/refresh conn index)
          (let [query (partial ads/query-ads-by-geo-distance conn index)]
            ;(is (= (:id index-res) (-> (query {:distance "100km" :lat 13.734603 :long 100.5639662}) :ids first)))
            )))
      (finally
        (component/stop system)))))










