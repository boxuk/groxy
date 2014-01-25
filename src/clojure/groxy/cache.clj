
(ns groxy.cache
  (:require [groxy.config :refer [config]]
            [groxy.cache.db :refer [database-cache-factory]]
            [clojure.tools.logging :refer [info]]
            [clojure.core.cache :as cache]))

(def cache-store
  (atom (cache/lru-cache-factory {})))

(defn- db-cache! [db]
  (info {:db-cache db})
  (reset! cache-store
          (database-cache-factory db)))

;; Public
;; ------

(def create-key (comp keyword str))

(defmacro with-key [cache-id & body]
  `(if (cache/has? @cache-store
                    ~cache-id)
      (get (cache/hit @cache-store ~cache-id)
           ~cache-id)
      (let [cache-data# (do ~@body)]
        (reset! cache-store
                (cache/miss @cache-store
                            ~cache-id
                            cache-data#))
        cache-data#)))

(defn init [config]
  (if-let [db-type (:db-cache-type config)]
    (db-cache!
      {:subprotocol db-type
       :subname (:db-cache-dsn config)
       :user (:db-cache-user config)
       :password (:db-cache-pass config)})))

