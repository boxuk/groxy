
(ns groxy.cache
  (:use [clojure.tools.logging :only [info]])
  (:require [groxy.config :refer [config]]
            [clojure.core.cache :as cache]))

(def cache-store
  (atom (cache/lru-cache-factory
          {}
          :threshold (:cachesize config))))

;; Public
;; ------

(def create-key (comp keyword str))

(defmacro with-key [cache-id & body]
  `(if (cache/has? @cache-store
                    ~cache-id)
      (do
        (info {:type "cache.hit"
               :key ~cache-id})
        (get (cache/hit @cache-store ~cache-id)
             ~cache-id))
      (do
        (info {:type "cache.miss"
               :key ~cache-id})
        (let [cache-data# (do ~@body)]
          (reset! cache-store
                  (cache/miss @cache-store
                              ~cache-id
                              cache-data#))
          cache-data#))))

