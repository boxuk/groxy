
(ns groxy.gmail
  (:use [clojure.tools.logging :only [info]])
  (:require [groxy.data :as data]
            [groxy.imap :as imap]
            [groxy.cache :as cache]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [ring.util.response :as response])
  (:import (javax.mail FetchProfile FetchProfile$Item Folder Multipart)
           (javax.mail.internet MimeMultipart)
           (com.sun.mail.imap IMAPMessage IMAPFolder IMAPBodyPart)
           (com.boxuk.groxy GmailSearchCommand)))

(def MAX_SEARCH_RESULTS 20)

(def error-message {:from "error@example.com"
                    :subject "Error parsing message"
                    :attachments []})

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

(defn- text-message [contents]
  (->> contents
       (filter string?)
       (first)))

(defn- html-message [contents]
  (if-let [part  (->> contents
                      (filter #(instance? MimeMultipart %))
                      (filter #(= "multipart/alternative" (content-type %)))
                      (first))]
    (.getContent
      (.getBodyPart part 1))))

(defn- message-body [^IMAPMessage msg]
  (let [c (.getContent msg)]
    (if (instance? Multipart c)
      (let [contents (->> (mime-parts msg)
                          (map #(.getContent %)))]
        (if-let [html (html-message contents)]
          html
          (text-message contents)))
      (str c))))

(defn- email2map [email]
  (let [[_ from address] (re-matches #"(.*)?<(.*)>" (.toString email))]
    {:name (string/trim (str from))
     :address address}))

;; Attachments
;; -----------

(defn- attachment2map [^IMAPBodyPart attachment]
  {:name (.getFileName attachment)
   :content-type (content-type attachment)})

(defn- with-id
  "Add an incrememting ID to each attachment"
  [acc attachment]
  (conj
    acc
    (assoc attachment
           :id
           (inc (count acc)))))

(defn- is-attachment? [^IMAPMessage msg]
  (and (not (is-plain-text msg))
       (not (= "multipart/alternative" (content-type msg)))))

(defn- attachments-for [^IMAPMessage msg]
  (->> (mime-parts msg)
       (filter is-attachment?)))

(defn- attachments [^IMAPMessage msg]
  (if (is-plain-text msg)
    []
    (->> (attachments-for msg)
         (map attachment2map)
         (reduce with-id []))))

(defn- message2map [^IMAPMessage msg]
  (try
    {:id (.getMessageNumber msg)
     :from (email2map (first (.getFrom msg)))
     :subject (.getSubject msg)
     :body (message-body msg)
     :attachments (attachments msg)}
    (catch Exception e
      (merge error-message
             {:id (.getMessageNumber msg)
              :body (.getMessage e)}))))

(defn- id2map [email ^IMAPFolder folder id]
  (cache/with-key
    (cache/create-key email "-" id)
    (message2map (imap/message folder id))))

(defn- content-stream-for
  [attachment]
  (let [stream (.getContent attachment)]
    (if (= MimeMultipart (class stream))
      ""
      stream)))

;; Store/Folder Handling
;; ---------------------

(defn- new-store-for [email token]
  (let [new-store (imap/store email token)]
    (info {:type "store.create"
           :email email})
    (dosync
      (alter data/stores assoc-in [email :store] new-store))
    new-store))

(defn- store-for [email token]
  (if-let [store (get-in @data/stores [email :store])]
    (if (.isConnected store)
        store
        (do (info {:type "store.close"
                   :email email}) (.close store)
            (new-store-for email token)))
    (new-store-for email token)))

(defn- folder-for* [folder-name email token]
  (let [folder (.getFolder
                 (store-for email token)
                 folder-name
                 )]
    (.open folder (Folder/READ_ONLY))
    folder))

(def allmail-for (partial folder-for* "[Gmail]/All Mail"))

(def inbox-for (partial folder-for* "INBOX"))

;; Search
;; ------

(defn- search* [email folder query]
  (let [command (GmailSearchCommand. (.getFullName folder) query)
        response (.doCommand folder command)
        ids (take MAX_SEARCH_RESULTS (.getMessageIds response))]
    (doall
      (map (partial id2map email folder) ids))))

;; Public
;; ------

(defn inbox [email token]
  (search*
    email
    (inbox-for email token)
    ""))

(defn search [email token query]
  (search*
    email
    (allmail-for email token)
    query))

(defn message [email token messageid]
  (id2map
    email
    (allmail-for email token)
    messageid))

(defn attachment [email token messageid attachmentid]
  (let [message (imap/message (allmail-for email token) messageid)
        attachment (nth (attachments-for message)
                        (dec attachmentid))]
    {:body (content-stream-for attachment)}))

