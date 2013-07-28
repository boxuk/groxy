
(ns groxy.web
  (:use compojure.core
        [clojure.tools.logging :only [info error]]
        [ring.util.response :only [response content-type status]]
        [net.cgrand.enlive-html :only [deftemplate]]
        [wrap-worker.core :only [wrap-worker]])
  (:require (compojure [handler :as handler]
                       [route :as route])
            [cheshire.core :as json]
            [groxy.gmail :as gmail]
            [groxy.stats :as stats]
            [clj-statsd :as s]))

(defn ->int [i]
  (Integer/parseInt i))

(defn millis []
  (System/currentTimeMillis))

(defmacro defhandler [fn-name params & body]
  `(defn ~fn-name [~@params]
     (try
       (do ~@body)
       (catch Exception e#
         (let [msg# (.getMessage e#)]
           (s/increment :exception-count)
           (error {:type "error"
                   :message msg#
                   :stack (map str (.getStackTrace e#))})
           (status (response {:message msg#})
                   400))))))

(defn wrap-logging [handler]
  (fn [req]
    (s/increment :request-count)
    (s/with-timing :request-time
      (handler req))))

(defn json-response [body]
  (-> (response (json/generate-string body))
      (content-type "application/json")))

(defn folder [params]
  (get params :folder gmail/FOLDER_ALL_MAIL))

;; WWW Pages
;; ---------

(deftemplate www-index "index.html" [req])

;; API Handlers
;; ------------

(defhandler api-stats [req]
  (json-response
    (stats/server)))

(defhandler api-search [{:keys [params]}]
  (s/increment :api-search)
  (s/with-timing :api-search-time
    (json-response
      (gmail/search
        (:email params)
        (:access_token params)
        (folder params)
        (:query params)))))

(defhandler api-message [{:keys [params]}]
  (s/increment :api-message)
  (s/with-timing :api-message-time
    (json-response
      (gmail/message
        (:email params)
        (:access_token params)
        (folder params)
        (->int (:messageid params))))))

(defhandler api-attachment [{:keys [params]}]
  (s/increment :api-attachment)
  (s/with-timing :api-attachment-time
    (gmail/attachment
      (:email params)
      (:access_token params)
      (folder params)
      (->int (:messageid params))
      (->int (:attachmentid params)))))

;; Routes
;; ------

(defroutes www-routes
  (GET "/" [] www-index)
  (GET "/release-version" [] (slurp "release-version"))
  (route/resources "/assets"))

(defroutes api-routes
  (context "/api" []
    (GET "/" [] api-stats)
    (GET "/messages" [] api-search)
    (GET "/messages/:messageid" [] api-message)
    (GET "/messages/:messageid/attachments/:attachmentid" [] api-attachment)))

(defroutes app-routes
  (routes
    www-routes
    api-routes
    (route/not-found "404")))

;; Public
;; ------

(def app
  (-> #'app-routes
    (wrap-logging)
    (wrap-worker)
    (handler/site)))

