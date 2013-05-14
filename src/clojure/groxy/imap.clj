
(ns groxy.imap
  (:use [clojure.tools.logging :only [info]])
  (:import (com.google.code.samples.oauth2 OAuth2SaslClientFactory OAuth2Authenticator)
           (java.util Properties)
           (javax.mail Session FetchProfile FetchProfile$Item)
           (com.sun.mail.imap IMAPSSLStore IMAPFolder)))

(OAuth2Authenticator/initialize)

(def GMAIL_IMAP_HOST "imap.gmail.com")
(def GMAIL_IMAP_PORT 993)

(defn- properties [token]
  (let [props (Properties.)]
     (doto props
      (.put "mail.imaps.sasl.enable" "true")
      (.put "mail.imaps.sasl.mechanisms", "XOAUTH2")
      (.put OAuth2SaslClientFactory/OAUTH_TOKEN_PROP token))
    props))

;; Public
;; ------

(defn store [email token]
  (let [session (Session/getInstance (properties token))
        store (IMAPSSLStore. session nil)]
    (info {:type "store.connect"
           :email email})
    (.connect store GMAIL_IMAP_HOST GMAIL_IMAP_PORT email "")
    store))

(defn message [^IMAPFolder folder id]
  (.getMessage folder id))

