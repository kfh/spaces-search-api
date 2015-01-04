(ns spaces-search-api-client.test.core
  (:require [clj-time.core :as time]
            [clj-time.coerce :as tc]
            [hara.common :refer [uuid]]
            [org.httpkit.client :as http]
            [spaces-search-api-client.core :as sapi]   
            [clojure.test :refer [deftest testing is]]
            [com.stuartsierra.component :as component]  
            [spaces-search-api-server.system :refer [spaces-test-system]]))

(deftest index-and-query-with-distance-filter
  (let [system (component/start (spaces-test-system))
        {:keys [web-server]} system]
    (try 
      (testing "Index and query using distance filter"
        (let [id (str (uuid))
              ts (tc/to-timestamp (time/now))
              geocodes {:lat 13.734603 :lon 100.5639662}
              url (str "http://" (:host web-server) ":" (:port web-server)) 
              [res error] (sapi/index url id ts geocodes)]
          (is (= nil error))
          (is (= id (-> res :body :id)))
          @(http/get (str url "/api/locations/refresh"))
          (let [q {:distance "5km" :lat 13.7175831 :long 100.5899095 :filter :distance-filter}
                [res error] (sapi/query url q)]
            (is (= nil error))
            (is (= 1 (-> res :body :hits)))
            (let [q {:distance "500m" :lat 13.7175831 :long 100.5899095 :filter :distance-filter}
                  [res error] (sapi/query url q)]
              (is (= nil error))
              (is (= 0 (-> res :body :hits)))))))
      (finally
        (component/stop system)))))

(deftest index-and-query-with-distance-range-filter
  (let [system (component/start (spaces-test-system))
        {:keys [web-server]} system]
    (try 
      (testing "Index and query using distance range filter"
        (let [id (str (uuid))
              ts (tc/to-timestamp (time/now))
              geocodes {:lat 13.734603 :lon 100.5639662}
              url (str "http://" (:host web-server) ":" (:port web-server)) 
              [res error] (sapi/index url id ts geocodes)]
          (is (= nil error))
          (is (= id (-> res :body :id)))
          @(http/get (str url "/api/locations/refresh"))
          (let [q {:from-distance "10km" :to-distance "50km" :lat 13.7175831 :long 100.5899095 :filter :distance-range-filter} 
                [res error] (sapi/query url q)]
            (is (= nil error))
            (is (= 0 (-> res :body :hits)))
            (let [q {:from-distance "400m" :to-distance "6km" :lat 13.7175831 :long 100.5899095 :filter :distance-range-filter} 
                  [res error] (sapi/query url q)]
              (is (= nil error))
              (is (= 1 (-> res :body :hits)))))))
      (finally
        (component/stop system)))))

(deftest index-and-query-with-polygon-filter
  (let [system (component/start (spaces-test-system))
        {:keys [web-server]} system]
    (try 
      (testing "Index and query using polygon filter"
        (let [id (str (uuid))
              ts (tc/to-timestamp (time/now))
              geocodes {:lat 13.734603 :lon 100.5639662}
              url (str "http://" (:host web-server) ":" (:port web-server)) 
              [res error] (sapi/index url id ts geocodes)]
          (is (= nil error))
          (is (= id (-> res :body :id)))
          @(http/get (str url "/api/locations/refresh"))
          (let [q {:lat-1 13.71 :long-1 100.55 :lat-2 13.70 :long-2 100.54 :lat-3 13.69 :long-3 100.53 :filter :polygon-filter} 
                [res error] (sapi/query url q)]
            (is (= nil error))
            (is (= 0 (-> res :body :hits)))
            (let [q {:lat-1 13.73 :long-1 100.59 :lat-2 13.71 :long-2 100.54 :lat-3 13.76 :long-3 100.54 :filter :polygon-filter} 
                  [res error] (sapi/query url q)]
              (is (= nil error))
              (is (= 1 (-> res :body :hits)))))))
      (finally
        (component/stop system)))))

(deftest index-and-query-with-bounding-box-filter
  (let [system (component/start (spaces-test-system))
        {:keys [web-server]} system]
    (try 
      (testing "Index and query using bounding box filter"
        (let [id (str (uuid))
              ts (tc/to-timestamp (time/now))
              geocodes {:lat 13.734603 :lon 100.5639662}
              url (str "http://" (:host web-server) ":" (:port web-server)) 
              [res error] (sapi/index url id ts geocodes)]
          (is (= nil error))
          (is (= id (-> res :body :id)))
          @(http/get (str url "/api/locations/refresh"))
          (let [q {:lat-top 13.7402 :long-top 100.5709 :lat-bottom 13.72 :long-bottom 100.5600 :filter :bounding-box-filter}
                [res error] (sapi/query url q)]
            (is (= nil error))
            (is (= 0 (-> res :body :hits)))
            (let [q {:lat-top 13.7390 :long-top 100.5540 :lat-bottom 13.7130 :long-bottom 100.5858 :filter :bounding-box-filter}
                  [res error] (sapi/query url q)]
              (is (= nil error))
              (is (= 1 (-> res :body :hits)))))))
      (finally
        (component/stop system)))))

(deftest index-and-update
  (let [system (component/start (spaces-test-system))
        {:keys [web-server]} system]
    (try 
      (testing "Index and update"
        (let [id (str (uuid))
              ts (tc/to-timestamp (time/now))
              geocodes {:lat 13.734603 :lon 100.5639662}
              url (str "http://" (:host web-server) ":" (:port web-server)) 
              [res error] (sapi/index url id ts geocodes)]
          (is (= nil error))
          (is (= id (-> res :body :id)))
          @(http/get (str url "/api/locations/refresh"))
          (let [[res error] (sapi/update url id {:ts ts :geocodes {:lat 13.896532 :lon 100.77885544}})]
            (is (= nil error))
            (is (= 201 (-> res :status)))
            (let [[res error] (sapi/get url id)]
              (is (= nil error))
              (is (= 13.896532 (-> res :body :source :geocodes :lat)))
              (is (= 100.77885544 (-> res :body :source :geocodes :lon)))))))
      (finally
        (component/stop system)))))

(deftest index-and-delete
  (let [system (component/start (spaces-test-system))
        {:keys [web-server]} system]
    (try 
      (testing "Index and delete"
        (let [id (str (uuid))
              ts (tc/to-timestamp (time/now))
              geocodes {:lat 13.734603 :lon 100.5639662}
              url (str "http://" (:host web-server) ":" (:port web-server))
              [res error] (sapi/index url id ts geocodes)]
          (is (= id (-> res :body :id)))
          @(http/get (str url "/api/locations/refresh"))
          (is (= 204 (let [[res error] (sapi/delete url id)] (:status res))))
          (is (= 404 (let [[res error] (sapi/get url id)] (:status error))))))
      (finally
        (component/stop system)))))
