
(ns groxy.imap
  (:import (com.google.code.samples.oauth2 OAuth2SaslClientFactory OAuth2Authenticator)
           (java.util Properties)
           (javax.mail Session Folder)
           (com.sun.mail.imap IMAPSSLStore))
  (:require [clojure.tools.logging :refer [info]]))

(OAuth2Authenticator/initialize)

(def GMAIL_IMAP_HOST "imap.gmail.com")
(def GMAIL_IMAP_PORT 993)

(def folders (atom {}))

(defn- properties [token]
  (doto (Properties.)
    (.put "mail.imaps.sasl.enable" "true")
    (.put "mail.imaps.sasl.mechanisms", "XOAUTH2")
    (.put OAuth2SaslClientFactory/OAUTH_TOKEN_PROP token)))

(defn- open-store [email token]
  (let [session (Session/getInstance (properties token))]
    (info {:type "store.connect"
           :email email})
    (doto (IMAPSSLStore. session nil)
      (.connect GMAIL_IMAP_HOST GMAIL_IMAP_PORT email ""))))

(defn- open-folder [email token folder-name]
  (info {:type "folder.open"
         :email email
         :folder-name folder-name})
  (let [folder (.getFolder (open-store email token) folder-name)]
    (.open folder Folder/READ_ONLY)
    (swap! folders assoc-in [email folder-name] folder)
    folder))

;; Public
;; ------

(defn folder [email token folder-name]
  (if-let [current-folder (get-in @folders [email folder-name])]
    (if (.isOpen current-folder)
      current-folder
      (open-folder email token folder-name))
    (open-folder email token folder-name)))

