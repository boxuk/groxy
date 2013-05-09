
(ns groxy.stats
  (:require [groxy.data :as data]
            [groxy.cache :as cache])
  (:import (java.lang.management ManagementFactory RuntimeMXBean)))

(defn store2map [k v]
  {:email k
   :connected (if-let [store (:store v)]
                (.isConnected (:store v)))})

(defn store-summary []
  (let [stores @data/stores]
    (map store2map
         (keys stores)
         (vals stores))))

;; Public
;; ------

(defn server []
  (let [rt (Runtime/getRuntime)]
    {:uptime (.getUptime (ManagementFactory/getRuntimeMXBean))
     :cache {:items (count (keys @cache/cache-store))}
     :memory {:total (.totalMemory rt)
              :free (.freeMemory rt)
              :max (.maxMemory rt)
              :used (- (.totalMemory rt) (.freeMemory rt))}
     :stores (store-summary)}))

