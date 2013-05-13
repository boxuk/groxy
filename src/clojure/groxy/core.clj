
(ns groxy.core
  (:use confo.core
        clojure.tools.logging
        clj-logging-config.log4j
        [org.httpkit.server :only [run-server]])
  (:require [groxy.web :as web])
  (:gen-class))

(def config (confo :groxy
                   :logfile "logs/access.log"
                   :port 4545))

(defn configure-logging []
  (org.apache.log4j.BasicConfigurator/configure)
  (set-logger! :level :debug
               :out (org.apache.log4j.DailyRollingFileAppender.
                      (org.apache.log4j.EnhancedPatternLayout.
                        org.apache.log4j.EnhancedPatternLayout/TTCC_CONVERSION_PATTERN)
                      (:logfile config)
                      "'.'yyyy-MM")
               :pattern "%d - %m%n"))

(defn start []
  (configure-logging)
  (run-server web/app config))

(defn -main []
  (start))

