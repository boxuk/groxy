
(ns groxy.config
  (:require [confo.core :refer [confo]]))

;; Public
;; ------

(def config (confo :groxy
                   ;; Server
                   :port 4545
                   ;; Logging
                   :logfile "logs/access.log"
                   :loglevel :debug
                   :logpattern "%d [%c: %l %n] %m\n"
                   ;; Caching
                   :cachesize 1000
                   ;; StatsD
                   :statsd-host "localhost"
                   :statsd-port 8125
                   ;; Database cache
                   :db-cache-type nil
                   :db-cache-dsn "//127.0.0.1:3307/db_name"
                   :db-cache-user "root"
                   :db-cache-pass ""))

