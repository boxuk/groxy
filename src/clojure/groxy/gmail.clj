
(ns groxy.gmail
  (:require [groxy.data :as data]
            [groxy.imap :as imap]
            [groxy.cache :as cache]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [ring.util.response :as response])
  (:import (javax.mail FetchProfile FetchProfile$Item)
           (javax.mail Folder)
           (com.sun.mail.imap IMAPMessage IMAPFolder)
           (com.boxuk.groxy GmailSearchCommand)))

(def MAX_SEARCH_RESULTS 20)

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

(defn- attachment2map [attachment]
  {:name (.getDescription attachment)
   :content-type (content-type attachment)})

(defn- with-id
  "Add an incrememting ID to each attachment"
  [acc attachment]
  (conj
    acc
    (assoc attachment
           :id
           (inc (count acc)))))

(defn- attachments [^IMAPMessage msg]
  (if (is-plain-text msg)
    []
    (->> (mime-parts msg)
         (filter (complement is-plain-text))
         (map attachment2map)
         (reduce with-id []))))

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

(defn- folder-for [email token]
  (let [store (store-for email token)
        ; the 'All Mail' folder always seems to be here...
        folder (-> store
                 (.getDefaultFolder)
                 (.list)
                 (second)
                 (.list)
                 (first))]
    (.open folder (Folder/READ_ONLY))
    folder))

;; Public
;; ------

(defn search [email token query]
  (let [all-mail (folder-for email token)
        search (GmailSearchCommand. (.getFullName all-mail) query)
        response (.doCommand all-mail search)
        ids (take MAX_SEARCH_RESULTS (.getMessageIds response))]
    (doall
      (map (partial id2map email all-mail) ids))))

(defn attachment [email token messageid attachmentid]
  (let [all-mail (folder-for email token)
        message (imap/message all-mail messageid)
        attachments (->> message
                         (mime-parts)
                         (filter (complement is-plain-text)))
        attachment (nth attachments attachmentid
                        (dec attachmentid))]
    {:body (.getContent attachment)}))

