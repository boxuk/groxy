
(ns groxy.stats
  (:require [fisher.core :as stats]
            [groxy.config :refer [config]]
            [groxy.cache :as cache]))

;; Public
;; ------

(defn server []
  (merge (stats/general)
         {:version (System/getProperty "groxy.version")
          :config config
          :release-version (slurp "release-version")
          :cache {:items (count (keys @cache/cache-store))}}))

