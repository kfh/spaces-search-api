(ns spaces-search-api.web.test.server
  (:require [hara.common :refer [uuid]]
            [clj-http.client :as http] 
            [cheshire.core :as json] 
            [clojure.test :refer [deftest testing is]]
            [com.stuartsierra.component :as component]  
            [spaces-search-api.system :refer [spaces-test-system]]))

(deftest index-and-query-location-with-distance-filter
  (let [system (component/start (spaces-test-system))
        {:keys [web-server]} system]
    (try 
      (testing "Index and query for location using distance filter"
        (let [loc {:id (str (uuid)) :geocodes {:lat 13.734603 :lon 100.5639662} :filter :distance-filter}
              url (str "http://" (:host web-server) ":" (:port web-server))
              post (partial http/post (str url "/api/locations"))
              res (post {:form-params loc :content-type :json})]
          (http/get (str url "/api/locations/refresh"))
          (is (= (:id loc) (:id (-> res :body (json/parse-string true)))))
          (let [query (partial http/post (str url "/api/locations/query"))
                query-params {:distance "5km" :lat 13.7175831 :long 100.5899095 :filter :distance-filter}
                query-res (query {:form-params query-params :content-type :json})]
            (is (= 1 (-> query-res :body (json/parse-string true) :hits)))
            (let [query-params {:distance "500m" :lat 13.7175831 :long 100.5899095 :filter :distance-filter}
                  query-res (query {:form-params query-params :content-type :json})]
              (is (= 0 (-> query-res :body (json/parse-string true) :hits)))))))
      (finally
        (component/stop system)))))

(deftest index-and-query-location-with-distance-range-filter
  (let [system (component/start (spaces-test-system))
        {:keys [web-server]} system]
    (try 
      (testing "Index and query for location using distance range filter"
        (let [loc {:id (str (uuid)) :geocodes {:lat 13.734603 :lon 100.5639662} :filter :distance-range-filter}
              url (str "http://" (:host web-server) ":" (:port web-server))
              post (partial http/post (str url "/api/locations"))
              res (post {:form-params loc :content-type :json})]
          (http/get (str url "/api/locations/refresh"))
          (is (= (:id loc) (:id (-> res :body (json/parse-string true)))))
          (let [query (partial http/post (str url "/api/locations/query"))
                query-params {:from-distance "10km" :to-distance "50km" :lat 13.7175831 :long 100.5899095 :filter :distance-range-filter}
                query-res (query {:form-params query-params :content-type :json})]
            (is (= 0 (-> query-res :body (json/parse-string true) :hits)))
            (let [query-params {:from-distance "400m" :to-distance "6km" :lat 13.7175831 :long 100.5899095 :filter :distance-range-filter}
                  query-res (query {:form-params query-params :content-type :json})]
              (is (= 1 (-> query-res :body (json/parse-string true) :hits)))))))
      (finally
        (component/stop system)))))


(deftest index-and-query-location-with-polygon-filter
  (let [system (component/start (spaces-test-system))
        {:keys [web-server]} system]
    (try 
      (testing "Index and query for location using polygon filter"
        (let [loc {:id (str (uuid)) :geocodes {:lat 13.734603 :lon 100.5639662} :filter :polygon-filter}
              url (str "http://" (:host web-server) ":" (:port web-server))
              post (partial http/post (str url "/api/locations"))
              res (post {:form-params loc :content-type :json})]
          (http/get (str url "/api/locations/refresh"))
          (is (= (:id loc) (:id (-> res :body (json/parse-string true)))))
          (let [query (partial http/post (str url "/api/locations/query"))
                query-params {:lat-1 13.71 :long-1 100.55 :lat-2 13.70 :long-2 100.54 :lat-3 13.69 :long-3 100.53 :filter :polygon-filter} 
                query-res (query {:form-params query-params :content-type :json})]
            (is (= 0 (-> query-res :body (json/parse-string true) :hits)))
            (let [query-params {:lat-1 13.73 :long-1 100.59 :lat-2 13.71 :long-2 100.54 :lat-3 13.76 :long-3 100.54 :filter :polygon-filter}
                  query-res (query {:form-params query-params :content-type :json})]
              (is (= 1 (-> query-res :body (json/parse-string true) :hits)))))))
      (finally
        (component/stop system)))))

(deftest index-and-query-location-with-bounding-box-filter
  (let [system (component/start (spaces-test-system))
        {:keys [web-server]} system]
    (try 
      (testing "Index and query for location using bounding box filter"
        (let [loc {:id (str (uuid)) :geocodes {:lat 13.734603 :lon 100.5639662} :filter :bounding-box-filter}
              url (str "http://" (:host web-server) ":" (:port web-server))
              post (partial http/post (str url "/api/locations"))
              res (post {:form-params loc :content-type :json})]
          (http/get (str url "/api/locations/refresh"))
          (is (= (:id loc) (:id (-> res :body (json/parse-string true)))))
          (let [query (partial http/post (str url "/api/locations/query"))
                query-params {:lat-top 13.7402 :long-top 100.5709 :lat-bottom 13.72 :long-bottom 100.5600 :filter :bounding-box-filter}
                query-res (query {:form-params query-params :content-type :json})]
            (is (= 0 (-> query-res :body (json/parse-string true) :hits)))
            (let [query-params  {:lat-top 13.7390 :long-top 100.5540 :lat-bottom 13.7130 :long-bottom 100.5858 :filter :bounding-box-filter}
                  query-res (query {:form-params query-params :content-type :json})]
              (is (= 1 (-> query-res :body (json/parse-string true) :hits)))))))
      (finally
        (component/stop system)))))
