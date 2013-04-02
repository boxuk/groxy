
(ns groxy.core
  (:use confo.core
        [org.httpkit.server :only [run-server]])
  (:require [groxy.web :as web]))

(def config (confo :groxy
                   :port 4545))

(defn start []
  (run-server web/app config))

(defn -main []
  (start))

