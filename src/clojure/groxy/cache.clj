
(ns groxy.cache)

(def data (ref {}))

;; Public
;; ------

(defn cache-write [id to-cache]
  (dosync
    (alter data assoc-in id to-cache)))

(defn cache-read [id]
  (get-in @data id))

(defmacro with-cache [id & body]
  `(if-let [cached# (cache-read ~id)]
     (with-meta cached# {:cached true})
     (let [to-cache# (doall ~@body)]
       (cache-write ~id to-cache#)
       (with-meta to-cache# {:cached false}))))

