
(ns groxy.stats
  (:require [groxy.data :as data])
  (:import (java.lang.management ManagementFactory RuntimeMXBean)))

(defn store2map [k v]
  {:email k
   :connected (.isConnected v)})

(defn stores []
  (let [stores @data/stores]
    (map store2map
         (keys stores)
         (vals stores))))

;; Public
;; ------

(defn server []
  (let [rt (Runtime/getRuntime)]
    {:uptime (.getUptime (ManagementFactory/getRuntimeMXBean))
     :memory {:total (.totalMemory rt)
              :free (.freeMemory rt)
              :max (.maxMemory rt)
              :used (- (.totalMemory rt) (.freeMemory rt))}
     :stores (stores)}))

