
(ns groxy.core
  (:use clojure.tools.logging
        clj-logging-config.log4j
        [ring.adapter.jetty :only [run-jetty]])
  (:require [groxy.config :refer [config]]
            [groxy.web :as web])
  (:gen-class))

(defn configure-logging []
  (set-logger! "groxy"
               :level (:loglevel config)
               :out (org.apache.log4j.RollingFileAppender.
                      (org.apache.log4j.EnhancedPatternLayout.
                        (:logpattern config))
                      (:logfile config)
                      true)))

(defn start []
  (configure-logging)
  (run-jetty web/app config))

(defn -main []
  (start))

