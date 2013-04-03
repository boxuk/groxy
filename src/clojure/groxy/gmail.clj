
(ns groxy.gmail
  (:require [groxy.data :as data])
  (:import (com.google.code.samples.oauth2 OAuth2SaslClientFactory OAuth2Authenticator)
           (java.util Properties)
           (javax.mail Session Folder FetchProfile FetchProfile$Item)
           (com.sun.mail.imap IMAPSSLStore)))

(OAuth2Authenticator/initialize)

(def GMAIL_IMAP_HOST "imap.gmail.com")
(def GMAIL_IMAP_PORT 993)

(defn- imap-properties [token]
  (let [props (Properties.)]
     (doto props
      (.put "mail.imaps.sasl.enable" "true")
      (.put "mail.imaps.sasl.mechanisms", "XOAUTH2")
      (.put OAuth2SaslClientFactory/OAUTH_TOKEN_PROP token))
    props))

(defn- imap-store [email token]
  (let [props (imap-properties token)
        session (Session/getInstance props)
        store (IMAPSSLStore. session nil)]
    (.connect store GMAIL_IMAP_HOST GMAIL_IMAP_PORT email "")
    store))

(defn- content-type [part]
  (let [ct (.getContentType part)]
    (.toLowerCase (.substring ct 0 (.indexOf ct ";")))))

(defn- is-plain-text [msg]
  (= "text/plain" (content-type msg)))

(defn- mime-parts [msg]
  (let [multipart (.getContent msg)]
    (for [i (range 0 (.getCount multipart))]
      (.getBodyPart multipart i))))

(defn- message-body [msg]
  (if (is-plain-text msg)
      (.getContent msg)
      (if-let [part (->> (mime-parts msg)
                         (filter is-plain-text)
                         (first))]
        (.getContent part)
        "")))

(defn- message-attachments [msg]
  (if (is-plain-text msg)
    []
    (->> (mime-parts msg)
         (filter (complement is-plain-text))
         (map attachment2map))))

(defn- email2map [email]
  {:address (.toString email)})

(defn- attachment2map [attachment]
  {:name (.getDescription attachment)
   :content-type (content-type attachment)})

(defn- message2map [msg]
  {:subject (.getSubject msg)
   :from (email2map (first (.getFrom msg)))
   :message-id (.getMessageID msg)
   :body (message-body msg)
   :attachments (message-attachments msg)})

(defn- new-store-for [email token]
  (let [new-store (imap-store email token)]
    (dosync
      (alter data/stores assoc-in [email] new-store))
    new-store))

(defn- store-for [email token]
  (if-let [store (get-in @data/stores [email])]
    (if (.isConnected store)
        store
        (doall (.close store)
               (new-store-for email token)))
    (new-store-for email token)))

;; Public
;; ------

(defn ^ {:doc "Return the Gmail inbox"}
  inbox [email token]
  (let [store (store-for email token)
        folder (.getFolder store "Inbox")]
    (.open folder (Folder/READ_ONLY))
    (let [msgs (.getMessages folder)
          profile (FetchProfile.)]
      (.add profile (FetchProfile$Item/ENVELOPE))
      (.add profile (FetchProfile$Item/CONTENT_INFO))
      (.fetch folder msgs profile)
      (map message2map msgs))))

