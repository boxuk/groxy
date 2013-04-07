
(ns groxy.util
  (:import (com.sun.mail.imap IMAPBodyPart)))

(defn worker-get [store id]
  (get-in @store id))

(defn worker-set [store id f]
  (dosync
    (alter store assoc-in id f)))

(defn worker-clear [store id]
  (worker-set store id nil))

(defn- method
  "Calls a private or protected method. (from clojure.contrib.java-utils)"
  [class-name method-name params obj & args]
  (-> class-name (.getDeclaredMethod (name method-name) (into-array Class params))
    (doto (.setAccessible true))
    (.invoke obj (into-array Object args))))

;; Public
;; ------

(defn base64
  "Return some Base64 encoded attachment content"
  [attachment]
  (let [stream (method IMAPBodyPart "getContentStream" [] attachment)]
    (slurp stream)))

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

