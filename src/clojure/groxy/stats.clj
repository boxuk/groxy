
(ns groxy.stats
  (:require [fisher.core :as stats]
            [groxy.data :as data]
            [groxy.cache :as cache]))

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
  (merge (stats/general)
         {:version (System/getProperty "groxy.version")
          :release-version (slurp "release-version")
          :cache {:items (count (keys @cache/cache-store))}
          :stores (store-summary)}))

