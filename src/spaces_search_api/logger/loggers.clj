(ns spaces-search-api.logger.loggers
  (:require [taoensso.timbre :as timbre]
            [com.stuartsierra.component :as component]
            [taoensso.timbre.appenders.rolling :as rolling]))

(timbre/refer-timbre)

(defrecord RollingFileAppender []
  component/Lifecycle

  (start [this]
    (info "Starting rolling file appender log")
    (if (:appender this)
      this  
      (let [config {:path "log/spaces-search-api-server.log" :pattern :daily}
            appender (rolling/make-rolling-appender)]
        (timbre/set-config! [:shared-appender-config :rolling] config)
        (timbre/set-config! [:appenders :rolling] appender)
        (assoc this :appender appender :config config))))

  (stop [this]
    (info "Stopping rolling file appender log")
    (if-not (:appender this)
      this
      (dissoc this :appender :config))))

(defn rolling-file-appender []
  (map->RollingFileAppender {}))

