
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
           (com.boxuk.groxy.gmail SearchCommand ThreadCommand)))

(def FOLDER_ALL_MAIL "[Gmail]/All Mail")

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

(defn- id->map [email ^IMAPFolder folder id]
  (cache/with-key
    (cache/create-key email "-" id)
      (->> (.getMessage folder id)
           (cail/message->map)
           (with-attachment-ids))))

(defn message-command [email token folder-name command]
  (let [folder (imap/folder email token folder-name)
        ids (.doCommand folder command)]
    (dmap (partial id->map email folder) ids)))

;; Public
;; ------

(defn search [email token folder-name query]
  (message-command
    email token folder-name
    (SearchCommand. folder-name query)))

(defn thread [email token folder-name message-id]
  (message-command
    email token folder-name
    (ThreadCommand. folder-name message-id)))

(defn message [email token folder-name message-id]
  (let [folder (imap/folder email token folder-name)]
    (id->map email folder message-id)))

(defn attachment [email token folder-name message-id attachment-id]
  (let [folder (imap/folder email token folder-name)
        message (.getMessage folder message-id)
        attachment (cail/with-content-stream
                     (cail/message->attachment message (dec attachment-id)))]
      (-> (:content-stream attachment)
          (response/response)
          (response/content-type
            (:content-type attachment)))))

