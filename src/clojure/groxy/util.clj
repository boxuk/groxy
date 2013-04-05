
(ns groxy.util)

(defn property
  "Access to private or protected field. (from clojure.contrib.java-utils)"
  [class-name field-name obj]
  (-> class-name (.getDeclaredField (name field-name))
    (doto (.setAccessible true))
    (.get obj)))

