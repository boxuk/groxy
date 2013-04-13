
(ns groxy.util
  (:import (com.sun.mail.imap IMAPBodyPart)))

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
  [^IMAPBodyPart attachment]
  (slurp
    (method IMAPBodyPart "getContentStream" [] attachment)))

