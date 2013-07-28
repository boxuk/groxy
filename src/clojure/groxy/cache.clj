
(ns groxy.cache
  (:require [groxy.config :refer [config]]
            [clojure.core.cache :as cache]
            [clj-statsd :as s]))

(def cache-store
  (atom (cache/lru-cache-factory
          {}
          :threshold (:cachesize config))))

(defn metric [id]
  (s/increment id)
  (s/gauge :cache-size (count (keys @cache-store))))

;; Public
;; ------

(def create-key (comp keyword str))

(defmacro with-key [cache-id & body]
  `(if (cache/has? @cache-store
                    ~cache-id)
      (do
        (metric :cache-hit)
        (get (cache/hit @cache-store ~cache-id)
             ~cache-id))
      (do
        (metric :cache-miss)
        (let [cache-data# (do ~@body)]
          (reset! cache-store
                  (cache/miss @cache-store
                              ~cache-id
                              cache-data#))
          cache-data#))))

