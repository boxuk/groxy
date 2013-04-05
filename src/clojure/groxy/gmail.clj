
(ns groxy.gmail
  (:require [groxy.data :as data]
            [clojure.string :as string])
  (:import (com.google.code.samples.oauth2 OAuth2SaslClientFactory OAuth2Authenticator)
           (java.util Properties)
           (javax.mail Session Folder FetchProfile FetchProfile$Item)
           (com.sun.mail.imap IMAPSSLStore)
           (com.boxuk.groxy GmailSearchCommand)))

(OAuth2Authenticator/initialize)

(def GMAIL_IMAP_HOST "imap.gmail.com")
(def GMAIL_IMAP_PORT 993)

(def MAX_SEARCH_RESULTS 20)

(def FOLDER_INBOX "Inbox")
(def FOLDER_ALLMAIL "[Gmail]/All Mail")

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

(defn- email2map [email]
  (let [[_ from address] (re-matches #"(.*)?<(.*)>" (.toString email))]
    {:name (string/trim from)
     :address address}))

(defn- attachment2map [attachment]
  {:name (.getDescription attachment)
   :content-type (content-type attachment)})

(defn- message-attachments [msg]
  (if (is-plain-text msg)
    []
    (->> (mime-parts msg)
         (filter (complement is-plain-text))
         (map attachment2map))))

(defn- message2map [msg]
  {:subject (.getSubject msg)
   :from (email2map (first (.getFrom msg)))
   :message-id (.getMessageID msg)
   :body (message-body msg)
   :attachments (message-attachments msg)})

(defn- new-store-for [email token]
  (let [new-store (imap-store email token)]
    (dosync
      (alter data/stores assoc-in [email :store] new-store))
    new-store))

(defn- store-for [email token]
  (if-let [store (get-in @data/stores [email :store])]
    (if (.isConnected store)
        store
        (doall (.close store)
               (new-store-for email token)))
    (new-store-for email token)))

(defn- folder-for [email token folder-name]
  (let [store (store-for email token)
        folder (.getFolder store folder-name)]
    (.open folder (Folder/READ_ONLY))
    folder))

(defn id2message [folder message-id]
  (.getMessage folder message-id))

;; Public
;; ------

(defn search [email token query]
  (let [all-mail (folder-for email token FOLDER_ALLMAIL)
        search (GmailSearchCommand. query)
        response (.doCommand all-mail search)
        ids (take MAX_SEARCH_RESULTS (.getMessageIds response))
        msgs (map (partial id2message all-mail) ids)]
    (map message2map msgs)))

(defn inbox [email token]
  (let [inbox (folder-for email token FOLDER_INBOX)
        msgs (.getMessages inbox)
        profile (doto (FetchProfile.)
                  (.add (FetchProfile$Item/ENVELOPE)))]
    (.fetch inbox msgs profile)
    (map message2map msgs)))

