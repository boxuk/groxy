
(ns groxy.core
  (:use confo.core
        clojure.tools.logging
        clj-logging-config.log4j
        [ring.adapter.jetty :only [run-jetty]])
  (:require [groxy.web :as web])
  (:gen-class))

(def config (confo :groxy
                   :logfile "logs/access.log"
                   :logpattern "%d [%c: %l %n] %m\n"
                   :port 4545))

(defn configure-logging []
  (set-logger! "groxy"
               :level :debug
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

