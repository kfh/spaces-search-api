(ns user
  (:require [clojure.repl :refer :all]
            [clojure.tools.namespace.repl :refer [refresh]]
            [spaces-search-api.system :as sys] 
            [com.stuartsierra.component :as component]))

(def system nil)

(defn init []
  (alter-var-root 
    #'system
    (constantly 
      (sys/spaces-system
        {:web-host "127.0.0.1"
         :web-port 4444}))))

(defn start []
  (alter-var-root #'system component/start))

(defn stop []
  (alter-var-root #'system
    (fn [s] (when s (component/stop s)))))

(defn go []
  (init)
  (start))

(defn reset []
  (stop)
  (refresh :after 'user/go))
