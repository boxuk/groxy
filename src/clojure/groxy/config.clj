
(ns groxy.config
  (:require [confo.core :refer [confo]]))

;; Public
;; ------

(def config (confo :groxy
                   :logfile "logs/access.log"
                   :loglevel :debug
                   :logpattern "%d [%c: %l %n] %m\n"
                   :port 4545
                   :cachesize 1000))

