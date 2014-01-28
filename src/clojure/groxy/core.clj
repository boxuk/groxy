
(ns groxy.core
  (:import (org.apache.log4j RollingFileAppender EnhancedPatternLayout))
  (:require [groxy.config :refer [config]]
            [groxy.cache :as cache]
            [groxy.web :as web]
            [groxy.metrics :as metrics]
            [clj-logging-config.log4j :refer [set-logger!]]
            [ring.adapter.jetty :refer [run-jetty]])
  (:gen-class))

(defn configure-logging []
  (set-logger! "groxy"
               :level (:loglevel config)
               :out (RollingFileAppender.
                      (EnhancedPatternLayout.
                        (:logpattern config))
                      (:logfile config)
                      true)))

(defn start []
  (configure-logging)
  (metrics/init config)
  (cache/init config)
  (run-jetty web/app config))

(defn -main []
  (start))

