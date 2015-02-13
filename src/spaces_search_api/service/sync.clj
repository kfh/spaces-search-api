(ns spaces-search-api.service.sync
  (:require [taoensso.timbre :as timbre]
            [com.stuartsierra.component :as component]  
            [clojure.core.async :refer [chan close!]]))

(timbre/refer-timbre)

