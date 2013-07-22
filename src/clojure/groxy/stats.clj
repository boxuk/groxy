
(ns groxy.stats
  (:require [fisher.core :as stats]
            [groxy.cache :as cache]))

;; Public
;; ------

(defn server []
  (merge (stats/general)
         {:version (System/getProperty "groxy.version")
          :release-version (slurp "release-version")
          :cache {:items (count (keys @cache/cache-store))}}))

