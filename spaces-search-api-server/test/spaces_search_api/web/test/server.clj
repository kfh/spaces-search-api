(ns spaces-search-api.web.test.server
  (:require [clj-time.core :as time]
            [clj-time.coerce :as tc]
            [hara.common :refer [uuid]]
            [clj-http.client :as http]
            [cognitect.transit :as transit]
            [clojure.test :refer [deftest testing is]]
            [com.stuartsierra.component :as component]  
            [spaces-search-api.system :refer [spaces-test-system]])
  (import [java.io ByteArrayOutputStream]))

(deftest index-and-query-location-with-distance-filter
  (let [system (component/start (spaces-test-system))
        {:keys [web-server]} system]
    (try 
      (testing "Index and query for location using distance filter"
        (let [ts-now (tc/to-timestamp (time/now))
              loc {:id (str (uuid)) :_timestamp ts-now :geocodes {:lat 13.734603 :lon 100.5639662}}
              url (str "http://" (:host web-server) ":" (:port web-server))
              post (partial http/post (str url "/api/locations"))
              res (post {:form-params loc :content-type :transit+json :as :transit+json})]
          (http/get (str url "/api/locations/refresh"))
          (is (= (:id loc) (-> res :body :id)))
          (let [query (partial http/get (str url "/api/locations/query"))
                query-params {:distance "5km" :lat 13.7175831 :long 100.5899095 :filter :distance-filter}
                buffer (ByteArrayOutputStream. 4096)
                writer (transit/writer buffer :json)
                _ (transit/write writer query-params)
                query-res (query {:accept :transit+json :body (str buffer) :as :transit+json})]
            (is (= 1 (-> query-res :body :hits)))
            (let [query-params {:distance "500m" :lat 13.7175831 :long 100.5899095 :filter :distance-filter}
                  _ (.reset buffer)
                  _ (transit/write writer query-params)
                  query-res (query {:accept :transit+json :body (str buffer) :as :transit+json})]
              (is (= 0 (-> query-res :body :hits)))))))
      (finally
        (component/stop system)))))

(deftest index-and-query-location-with-distance-range-filter
  (let [system (component/start (spaces-test-system))
        {:keys [web-server]} system]
    (try 
      (testing "Index and query for location using distance range filter"
        (let [ts-now (tc/to-timestamp (time/now))
              loc {:id (str (uuid)) :_timestamp ts-now :geocodes {:lat 13.734603 :lon 100.5639662}}
              url (str "http://" (:host web-server) ":" (:port web-server))
              post (partial http/post (str url "/api/locations"))
              res (post {:form-params loc :content-type :transit+json :as :transit+json})]
          (http/get (str url "/api/locations/refresh"))
          (is (= (:id loc) (-> res :body :id)))
          (let [query (partial http/get (str url "/api/locations/query"))
                query-params {:from-distance "10km" :to-distance "50km" :lat 13.7175831 :long 100.5899095 :filter :distance-range-filter}
                buffer (ByteArrayOutputStream. 4096)
                writer (transit/writer buffer :json)
                _ (transit/write writer query-params)
                query-res (query {:accept :transit+json :body (str buffer) :as :transit+json})]
            (is (= 0 (-> query-res :body :hits)))
            (let [query-params {:from-distance "400m" :to-distance "6km" :lat 13.7175831 :long 100.5899095 :filter :distance-range-filter}
                  _ (.reset buffer)
                  _ (transit/write writer query-params)
                  query-res (query {:accept :transit+json :body (str buffer) :as :transit+json})]
              (is (= 1 (-> query-res :body :hits)))))))
      (finally
        (component/stop system)))))

(deftest index-and-query-location-with-polygon-filter
  (let [system (component/start (spaces-test-system))
        {:keys [web-server]} system]
    (try 
      (testing "Index and query for location using polygon filter"
        (let [ts-now (tc/to-timestamp (time/now))
              loc {:id (str (uuid)) :_timestamp ts-now :geocodes {:lat 13.734603 :lon 100.5639662}}
              url (str "http://" (:host web-server) ":" (:port web-server))
              post (partial http/post (str url "/api/locations"))
              res (post {:form-params loc :content-type :transit+json :as :transit+json})]
          (http/get (str url "/api/locations/refresh"))
          (is (= (:id loc) (-> res :body :id)))
          (let [query (partial http/get (str url "/api/locations/query"))
                query-params {:lat-1 13.71 :long-1 100.55 :lat-2 13.70 :long-2 100.54 :lat-3 13.69 :long-3 100.53 :filter :polygon-filter} 
                buffer (ByteArrayOutputStream. 4096)
                writer (transit/writer buffer :json)
                _ (transit/write writer query-params)
                query-res (query {:accept :transit+json :body (str buffer) :as :transit+json})]
            (is (= 0 (-> query-res :body :hits)))
            (let [query-params {:lat-1 13.73 :long-1 100.59 :lat-2 13.71 :long-2 100.54 :lat-3 13.76 :long-3 100.54 :filter :polygon-filter}
                  _ (.reset buffer)
                  _ (transit/write writer query-params)
                  query-res (query {:accept :transit+json :body (str buffer) :as :transit+json})]
              (is (= 1 (-> query-res :body :hits)))))))
      (finally
        (component/stop system)))))

(deftest index-and-query-location-with-bounding-box-filter
  (let [system (component/start (spaces-test-system))
        {:keys [web-server]} system]
    (try 
      (testing "Index and query for location using bounding box filter"
        (let [ts-now (tc/to-timestamp (time/now))
              loc {:id (str (uuid)) :_timestamp ts-now :geocodes {:lat 13.734603 :lon 100.5639662}}
              url (str "http://" (:host web-server) ":" (:port web-server))
              post (partial http/post (str url "/api/locations"))
              res (post {:form-params loc :content-type :transit+json :as :transit+json})]
          (http/get (str url "/api/locations/refresh"))
          (is (= (:id loc) (-> res :body :id)))
          (let [query (partial http/get (str url "/api/locations/query"))
                query-params {:lat-top 13.7402 :long-top 100.5709 :lat-bottom 13.72 :long-bottom 100.5600 :filter :bounding-box-filter}
                buffer (ByteArrayOutputStream. 4096)
                writer (transit/writer buffer :json)
                _ (transit/write writer query-params)
                query-res (query {:accept :transit+json :body (str buffer) :as :transit+json})]
            (is (= 0 (-> query-res :body :hits)))
            (let [query-params  {:lat-top 13.7390 :long-top 100.5540 :lat-bottom 13.7130 :long-bottom 100.5858 :filter :bounding-box-filter}
                  _ (.reset buffer)
                  _ (transit/write writer query-params)
                  query-res (query {:accept :transit+json :body (str buffer) :as :transit+json})]
              (is (= 1 (-> query-res :body :hits)))))))
      (finally
        (component/stop system)))))

(deftest index-and-update-location
  (let [system (component/start (spaces-test-system))
        {:keys [web-server]} system]
    (try 
      (testing "Index and update location"
        (let [ts-now (tc/to-timestamp (time/now))
              loc {:id (str (uuid)) :_timestamp ts-now :geocodes {:lat 13.734603 :lon 100.5639662}}
              url (str "http://" (:host web-server) ":" (:port web-server))
              post (partial http/post (str url "/api/locations"))
              res (post {:form-params loc :content-type :transit+json :as :transit+json})]
          (http/get (str url "/api/locations/refresh"))
          (is (= (:id loc) (-> res :body :id)))
          (let [update-location (partial http/put (str url "/api/locations/" (:id loc)))]
            (update-location {:form-params {:id (:id loc) :_timestamp (:_timestamp loc) :geocodes {:lat 13.896532 :lon 100.77885544}} :content-type :transit+json})
            (let [updated-location (http/get (str url "/api/locations/" (:id loc)) {:as :transit+json})]
              (is (= 13.896532  (-> updated-location :body :source :geocodes :lat)))
              (is (= 100.77885544 (-> updated-location :body :source :geocodes :lon)))))))   
      (finally
        (component/stop system)))))

(deftest index-and-delete-location
  (let [system (component/start (spaces-test-system))
        {:keys [web-server]} system]
    (try 
      (testing "Index and delete location"
        (let [ts-now (tc/to-timestamp (time/now))
              loc {:id (str (uuid)) :_timestamp ts-now :geocodes {:lat 13.734603 :lon 100.5639662}}
              url (str "http://" (:host web-server) ":" (:port web-server))
              post (partial http/post (str url "/api/locations"))
              res (post {:form-params loc :content-type :transit+json :as :transit+json})]
          (http/get (str url "/api/locations/refresh"))
          (is (= (:id loc) (-> res :body :id)))
          (is (= 204 (-> (http/delete (str url "/api/locations/" (:id loc))) :status)))
          (is (= 404 (-> (http/get (str url "/api/locations/" (:id loc)) {:throw-exceptions false}) :status)))))
      (finally
        (component/stop system)))))
