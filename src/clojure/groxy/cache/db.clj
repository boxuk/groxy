
(ns groxy.cache.db
  (:require [clojure.core.cache :refer [defcache CacheProtocol]]
            [clojure.java.jdbc :refer [query insert!]]))

(def cache-table :groxy_cache)

(defn find-row [db id]
  (let [sql (format "select id, data from %s where id = ?"
                    (name cache-table))]
    (query db [sql (name id)])))

(defn lookup
  ([db id] (lookup db id nil))
  ([db id not-found]
   (let [res (find-row db id)]
     (if-let [row (first res)]
       (read-string (:data row))
       not-found))))

(defn store [db id data]
  (insert! db cache-table
           {:id (name id)
            :data (pr-str data)}))

(defn purge [db id])

(defcache DatabaseCache [db]
  CacheProtocol
  (lookup [_ e]
          (lookup db e))
  (has? [_ e]
        (not (nil? (lookup db e))))
  (hit [_ e]
       (hash-map e (lookup db e)))
  (miss [_ e ret]
        (store db e ret)
        _)
  (evict [_ e]
         (purge db e))
  (seed [_ base]
        (throw (Exception. "Seeding not supported"))))

;; Public
;; ------

(defn database-cache-factory
  "A durable JDBC database backed cache."
  [db]
  (DatabaseCache. db))

