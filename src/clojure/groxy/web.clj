
(ns groxy.web
  (:use compojure.core
        ring.middleware.reload
        ring.middleware.stacktrace
        [clojure.tools.logging :only [info error]]
        [ring.util.response :only [response content-type status]]
        [net.cgrand.enlive-html :only [deftemplate]]
        [wrap-worker.core :only [wrap-worker]])
  (:require (compojure [handler :as handler]
                       [route :as route])
            [cheshire.core :as json]
            [groxy.gmail :as gmail]
            [groxy.stats :as stats]))

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

(defn wrap-logging [handler]
  (fn [req]
    (let  [start-time (millis)
           res (handler req)]
      (info (merge {:type "request"
                    :start-time start-time
                    :total-time (- (millis) start-time)}
                   (select-keys req [:request-method :uri :params])))
      res)))

(defn json-response [body]
  (-> (response (json/generate-string body))
      (content-type "application/json")))

;; WWW Pages
;; ---------

(deftemplate www-index "index.html" [req])

;; API Handlers
;; ------------

(defhandler api-stats [req]
  (json-response
    (stats/server)))

(defhandler api-inbox [{:keys [params]}]
  (json-response
    (gmail/inbox
      (:email params)
      (:access_token params))))

(defhandler api-search [{:keys [params]}]
  (json-response
    (gmail/search
      (:email params)
      (:access_token params)
      (:query params))))

(defhandler api-message [{:keys [params]}]
  (json-response
    (gmail/message
      (:email params)
      (:access_token params)
      (->int (:messageid params)))))

(defhandler api-attachment [{:keys [params]}]
  (gmail/attachment
    (:email params)
    (:access_token params)
    (->int (:messageid params))
    (->int (:attachmentid params))))

;; Routes
;; ------

(defroutes www-routes
  (GET "/" [] www-index)
  (route/resources "/assets"))

(defroutes api-routes
  (context "/api" []
    (GET "/" [] api-stats)
    (GET "/inbox" [] api-inbox)
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
    (wrap-reload)
    (wrap-stacktrace)
    (wrap-logging)
    (wrap-worker)
    (handler/site)))

