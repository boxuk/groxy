
(ns groxy.gmail
  (:use [clojure.tools.logging :only [info]])
  (:require [groxy.imap :as imap]
            [groxy.cache :as cache]
            [cail.core :as cail]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [clojure.tools.logging :refer [info]]
            [ring.util.response :as response])
  (:import (com.sun.mail.imap IMAPFolder)
           (com.boxuk.groxy GmailSearchCommand)))

(def FOLDER_ALL_MAIL "[Gmail]/All Mail")
(def MAX_SEARCH_RESULTS 20)

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
      (->> (.getMessage folder id)
           (cail/message->map)
           (with-attachment-ids))))

;; Public
;; ------

(defn search [email token folder-name query]
  (let [folder (imap/folder email token FOLDER_ALL_MAIL)
        command (GmailSearchCommand. FOLDER_ALL_MAIL query)
        response (.doCommand folder command)
        ids (take MAX_SEARCH_RESULTS (.getMessageIds response))]
    (dmap (partial id2map email folder) ids)))

(defn message [email token folder-name messageid]
  (let [folder (imap/folder email token FOLDER_ALL_MAIL)]
    (id2map email folder messageid)))

(defn attachment [email token folder-name messageid attachmentid]
  (let [folder (imap/folder email token FOLDER_ALL_MAIL)
        message (.getMessage folder messageid)
        attachment (cail/with-content-stream
                     (cail/message->attachment message (dec attachmentid)))]
      (-> (:content-stream attachment)
          (response/response)
          (response/content-type
            (:content-type attachment)))))

