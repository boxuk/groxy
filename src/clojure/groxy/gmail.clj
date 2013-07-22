
(ns groxy.gmail
  (:use [clojure.tools.logging :only [info]])
  (:require [groxy.imap :as imap]
            [groxy.cache :as cache]
            [cail.core :as cail]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [clojure.tools.logging :refer [info]]
            [ring.util.response :as response])
  (:import (javax.mail FetchProfile FetchProfile$Item Folder Multipart)
           (javax.mail.internet MimeMultipart)
           (com.sun.mail.imap IMAPMessage IMAPFolder IMAPBodyPart)
           (com.boxuk.groxy GmailSearchCommand)))

(def FOLDER_ALL_MAIL "[Gmail]/All Mail")
(def MAX_SEARCH_RESULTS 20)

(def folders (atom {}))

(def dmap (comp doall map))

(defn- with-id
  "Add an incrememting ID to each attachment"
  [acc attachment]
  (conj
    acc
    (assoc attachment
           :id
           (inc (count acc)))))

(defn- with-attachment-ids [msg]
  (assoc
    msg
    :attachments
    (reduce with-id [] (:attachments msg))))

(defn- id2map [email ^IMAPFolder folder id]
  (cache/with-key
    (cache/create-key email "-" id)
      (->> (imap/message folder id)
           (cail/message->map)
           (with-attachment-ids))))

;; Folder Handling
;; ---------------------

(defn- open-folder [email token folder-name]
  (info {:type "folder.open"
         :email email
         :folder-name folder-name})
  (let [folder (.getFolder (imap/store email token) folder-name)]
    (.open folder Folder/READ_ONLY)
    (swap! folders assoc-in [email folder-name] folder)
    folder))

(defn- get-folder [email token folder-name]
  (if-let [folder (get-in @folders [email folder-name])]
    (if (.isOpen folder)
      folder
      (open-folder email token folder-name))
    (open-folder email token folder-name)))

(defn search-folder
  ([] (search-folder ""))
  ([message-filter]
    (fn [email token query]
      (let [folder (get-folder email token FOLDER_ALL_MAIL)
            message-query (str message-filter " " query)
            command (GmailSearchCommand. FOLDER_ALL_MAIL message-query)
            response (.doCommand folder command)
            ids (take MAX_SEARCH_RESULTS (.getMessageIds response))]
        (dmap (partial id2map email folder) ids)))))

;; Public
;; ------

(def inbox (search-folder "label:inbox"))

(def search (search-folder))

(defn message [email token messageid]
  (let [allmail (get-folder email token FOLDER_ALL_MAIL)]
    (id2map email allmail messageid)))

(defn attachment [email token messageid attachmentid]
  (let [allmail (get-folder email token FOLDER_ALL_MAIL)
        message (imap/message allmail messageid)
        attachment (cail/with-content-stream
                     (cail/message->attachment message (dec attachmentid)))]
      (-> (:content-stream attachment)
          (response/response)
          (response/content-type
            (:content-type attachment)))))

