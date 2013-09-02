
(ns groxy.core
  (:use clojure.tools.logging
        clj-logging-config.log4j
        [ring.adapter.jetty :only [run-jetty]])
  (:require [groxy.config :refer [config]]
            [groxy.cache :refer [db-cache!]]
            [groxy.web :as web]
            [clj-statsd :as s])
  (:gen-class))

(defn configure-logging []
  (set-logger! "groxy"
               :level (:loglevel config)
               :out (org.apache.log4j.RollingFileAppender.
                      (org.apache.log4j.EnhancedPatternLayout.
                        (:logpattern config))
                      (:logfile config)
                      true)))

(defn configure-statsd []
  (s/setup
    (:statsd-host config)
    (:statsd-port config)
    :prefix "groxy."))

(defn configure-cache []
  (if-let [db-type (:db-cache-type config)]
    (db-cache!
      {:subprotocol db-type
       :subname (:db-cache-dsn config)
       :user (:db-cache-user config)
       :password (:db-cache-pass config)})))

(defn start []
  (configure-logging)
  (configure-statsd)
  (configure-cache)
  (run-jetty web/app config))

(defn -main []
  (start))

