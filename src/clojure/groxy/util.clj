
(ns groxy.util
  (:require [ring.util.codec :as codec]))

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

