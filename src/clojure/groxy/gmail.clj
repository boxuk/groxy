
(ns groxy.gmail
  (:require [groxy.data :as data]
            [groxy.util :as util]
            [groxy.imap :as imap]
            [groxy.cache :as cache]
            [clojure.string :as string])
  (:import (javax.mail FetchProfile FetchProfile$Item)
           (javax.mail Folder)
           (com.sun.mail.imap IMAPMessage IMAPFolder)
           (com.boxuk.groxy GmailSearchCommand)))

(def MAX_SEARCH_RESULTS 20)

;(def FOLDER_GOOGLEMAIL "[Google Mail]/All Mail")
(def FOLDER_GMAIL "[Gmail]/All Mail")

;; Message Parsing
;; ---------------

(defn- content-type [part]
  (let [ct (.getContentType part)]
    (if (.contains ct ";")
      (.toLowerCase (.substring ct 0 (.indexOf ct ";")))
      "text/plain")))

(defn- is-plain-text [^IMAPMessage msg]
  (let [ct (content-type msg)]
    (or (= "text/plain" ct)
        (= "text/html" ct))))

(defn- mime-parts [^IMAPMessage msg]
  (let [multipart (.getContent msg)]
    (for [i (range 0 (.getCount multipart))]
      (.getBodyPart multipart i))))

(defn- message-body [^IMAPMessage msg]
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

(defn- attachment2map [with-data attachment]
  {:name (.getDescription attachment)
   :content-type (content-type attachment)
   :data (if with-data
           (util/base64 attachment))})

(defn- attachments [^IMAPMessage msg & [options]]
  (if (is-plain-text msg)
    []
    (->> (mime-parts msg)
         (filter (complement is-plain-text))
         (map (partial attachment2map (:with-data options))))))

(defn- message2map [^IMAPMessage msg]
  {:id (.getMessageNumber msg)
   :from (email2map (first (.getFrom msg)))
   :subject (.getSubject msg)
   :body (message-body msg)
   :attachments (attachments msg)})

(defn- id2map [email ^IMAPFolder folder id]
  (cache/with-key
    (cache/create-key email "-" id)
    (message2map (imap/message folder id))))

;; Store/Folder Handling
;; ---------------------

(defn- new-store-for [email token]
  (let [new-store (imap/store email token)]
    (dosync
      (alter data/stores assoc-in [email :store] new-store))
    new-store))

(defn- store-for [email token]
  (if-let [store (get-in @data/stores [email :store])]
    (if (.isConnected store)
        store
        (do (.close store)
            (new-store-for email token)))
    (new-store-for email token)))

(defn- folder-for [email token folder-name]
  (let [store (store-for email token)
        folder (.getFolder store folder-name)]
    (.open folder (Folder/READ_ONLY))
    folder))

;; Public
;; ------

(defn search [email token query]
  (let [all-mail (folder-for email token FOLDER_GMAIL)
        search (GmailSearchCommand. FOLDER_GMAIL query)
        response (.doCommand all-mail search)
        ids (take MAX_SEARCH_RESULTS (.getMessageIds response))]
    (doall
      (map (partial id2map email all-mail) ids))))

(defn message [email token id]
  (let [all-mail (folder-for email token FOLDER_GMAIL)
        data (id2map email all-mail id)
        attchs (attachments (imap/message all-mail id)
                            {:with-data true})]
    (merge data {:attachments attchs})))

