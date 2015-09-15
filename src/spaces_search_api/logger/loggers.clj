(ns spaces-search-api.logger.loggers
  (:require [taoensso.timbre :as timbre]
            [com.stuartsierra.component :as component]
            [taoensso.timbre.appenders.core :as core] 
            [taoensso.timbre.appenders.3rd-party.rolling :as rolling]))

(timbre/refer-timbre)

(defrecord RollingFileAppender []
  component/Lifecycle

  (start [this]
    (info "Starting rolling file appender log")
    (if (:appender this)
      this  
      (let [printline (core/println-appender {:stream :auto}) 
            rolling-opts {:path "log/spaces-search-api.log" :pattern :daily} 
            rolling (rolling/rolling-appender rolling-opts)
            config {:level :info :appenders {:rolling rolling :println printline}}]
        (timbre/set-config! config)
        (assoc this :config config))))

  (stop [this]
    (info "Stopping rolling file appender log")
    (if-not (:config this)
      this
      (dissoc this :config))))

(defn rolling-file-appender []
  (map->RollingFileAppender {}))

