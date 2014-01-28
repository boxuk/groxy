
(ns groxy.web
  (:require [groxy.metrics :refer [wrap-metrics]]
            [groxy.gmail :as gmail]
            [groxy.stats :as stats]
            [compojure.core :refer :all]
            (compojure [handler :as handler]
                       [route :as route])
            [cheshire.core :as json]
            [clojure.tools.logging :refer [info error]]
            [ring.util.response :refer [response content-type status]]
            [net.cgrand.enlive-html :refer [deftemplate]]
            [wrap-worker.core :refer [wrap-worker]]))

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
           (error {:type "error"
                   :message msg#
                   :stack (map str (.getStackTrace e#))})
           (status (response {:message msg#})
                   400))))))

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
  (json-response
    (gmail/search
      (:email params)
      (:access_token params)
      (folder params)
      (:query params))))

(defhandler api-message [{:keys [params]}]
  (json-response
    (gmail/message
      (:email params)
      (:access_token params)
      (folder params)
      (->int (:messageid params)))))

(defhandler api-attachment [{:keys [params]}]
  (gmail/attachment
    (:email params)
    (:access_token params)
    (folder params)
    (->int (:messageid params))
    (->int (:attachmentid params))))

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
    (wrap-metrics)
    (wrap-worker)
    (handler/site)))

