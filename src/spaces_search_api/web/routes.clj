(ns spaces-search-api.web.routes
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [taoensso.timbre :as timbre]
            [spaces-search-api.service.ads :as service]   
            [com.stuartsierra.component :as component]))

(timbre/refer-timbre)

(defn- query-ads [db query]
  (let [{:keys [conn index]} db] 
    (if-let [ads (service/query-ads conn index query)]
      {:status 200 :body ads}
      {:status 404})))

(defn- index-ad [db req]
  (let [{:keys [conn index]} db] 
    {:status 201
     :body (service/index-ad conn index (:params req))}))

(defn- update-ad [db req ad-id]
  (let [{:keys [conn index]} db] 
    {:status 201
     :body (service/update-ad conn index (:params req) ad-id)}))

(defn- delete-ad [db ad-id]
  (let [{:keys [conn index]} db] 
    (if (service/delete-ad conn index ad-id)
      {:status 204}
      {:status 404})))

(defrecord ApiRoutes [es]
  component/Lifecycle

  (start [this]
    (info "Enabling api routes")
    (if (:routes this)
      this 
      (let [api-routes (routes
                         (GET "/ads/:query" [query :as req] (query-ads es query))
                         (POST "/ads" req (index-ad es req))
                         (PUT "/ads/:ad-id" [ad-id :as req] (update-ad es req ad-id))
                         (DELETE "/ads/:ad-id" [ad-id :as req] (delete-ad ad-id))
                         (route/resources "/")
                         (route/not-found "Not Found"))]
        (assoc this :routes api-routes))))

  (stop [this]
    (info "Disabling api routes")
    (if-not (:routes this) 
      this
      (dissoc this :routes))))

(defn api-routes []
  (component/using
    (map->ApiRoutes {})
    [:es]))
