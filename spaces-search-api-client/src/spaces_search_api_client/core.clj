(ns spaces-search-api-client.core
  (:require [ribol.core :refer [raise]]
            [org.httpkit.client :as http]
            [cognitect.transit :as transit]
            [clojure.core.async :refer [<!! chan put!]])
  (import [java.io ByteArrayInputStream ByteArrayOutputStream]))

(def ok #{200 201 202 203 204 205 206 207 300 301 302 303 307})

(defn- read-transit [data]
  (-> data
      (.getBytes)
      (ByteArrayInputStream.)
      (transit/reader :json)
      (transit/read)))

(defn- write-transit [data]
  (let [buffer (ByteArrayOutputStream. 4096)
        writer (transit/writer buffer :json)]
    (transit/write writer data)
    buffer))

(defn- do-req 
  ([method url] 
   (do-req method url nil))
  ([method url body]  
   (let [c (chan 1)] 
     (http/request 
       (cond->
         {:url url 
          :method method}
         body (assoc :body (str (write-transit body)))
         (or (= :put method) (= :post method)) 
           (assoc :headers {"Content-Type" "application/transit+json"})) 
       (fn [req] (put! c req)))
     c)))

(defn- handle-resp [channel]
  (let [{:keys [status body error]} (<!! channel)]
    (when error (raise {:error error}))
    (if (contains? ok status)
      [{:status status 
        :body (if (= 204 status) 
                body
                (read-transit body))} nil]
      [nil {:status status 
            :error (if (contains? #{400 404} status) 
                     body
                     (read-transit error))}])))

(defn index [url id ts geocodes]
  (let [url (str url "/api/locations")
        data {:id id :_timestamp ts :geocodes geocodes}] 
    (->> data
         (do-req :post url)
         (handle-resp))))

(defn query [url query]
  (let [url (str url "/api/locations/query")] 
    (->> query
         (do-req :get url)
         (handle-resp))))

(defn update [url id data]
  (let [url (str url "/api/locations/" id)
        data {:id id :_timestamp (:ts data) :geocodes (:geocodes data)}] 
    (->> data
         (do-req :put url)
         (handle-resp))))

(defn delete [url id]
  (let [url (str url "/api/locations/" id)]
    (->> url
        (do-req :delete)
        (handle-resp))))

(defn get [url id]
  (let [url (str url "/api/locations/" id)]
    (->> url 
         (do-req :get)
         (handle-resp))))
