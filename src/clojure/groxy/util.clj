
(ns groxy.util
  (:require [ring.util.codec :as codec]))

(defn worker-get [store id]
  (get-in @store id))

(defn worker-set [store id f]
  (dosync
    (alter store assoc-in id f)))

(defn worker-clear [store id]
  (worker-set store id nil))

;; Public
;; ------

(defn property
  "Access to private or protected field. (from clojure.contrib.java-utils)"
  [class-name field-name obj]
  (-> class-name (.getDeclaredField (name field-name))
    (doto (.setAccessible true))
    (.get obj)))

(defn base64
  "Return some Base64 encoded attachment content"
  [attachment]
  (let [binary (slurp (.getContent attachment))]
    (codec/base64-encode (.getBytes binary))))

(defmacro worker
  "Use futures to handle waiting on tasks already being processed"
  [store id & body]
  `(if-let [waiting# (worker-get ~store ~id)]
     (with-meta @waiting# {:via-worker false})
     (let [f# (future ~@body)]
       (worker-set ~store ~id f#)
       (try
         (with-meta @f# {:via-worker true})
         (finally
           (worker-clear ~store ~id))))    ))

