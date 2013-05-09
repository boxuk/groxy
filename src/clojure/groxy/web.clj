
(ns groxy.web
  (:use compojure.core
        ring.middleware.reload
        ring.middleware.stacktrace
        [clojure.tools.logging :only [info error]]
        [ring.util.response :only [response status]]
        [net.cgrand.enlive-html :only [deftemplate]]
        [wrap-worker.core :only [wrap-worker]]
        [ring.middleware.format-response :only [wrap-json-response]])
  (:require (compojure [handler :as handler]
                       [route :as route])
            [groxy.gmail :as gmail]
            [groxy.stats :as stats]))

(defmacro defhandler [fn-name params & body]
  `(defn ~fn-name [~@params]
     (try
       (response (do ~@body))
       (catch Exception e#
         (let [msg# (.getMessage e#)
               error# (response {:message msg#})]
           (error "Error:" msg#)
           (error (map str (.getStackTrace e#)))
           (status error# 400))))))

(defn params-for [req]
  (let [params (:params req) ]
    [(:email params)
     (:access_token params)
     (:query params)]))

(defn wrap-logging [handler]
  (fn [req]
    (info (merge
            {:type "request"}
            (select-keys req [:request-method :uri :params])))
    (time
      (handler req))))

;; WWW Pages
;; ---------

(deftemplate www-index "index.html" [req])

;; API Handlers
;; ------------

(defhandler api-stats [req]
  (stats/server))

(defhandler api-search [req]
  (let [[email token query] (params-for req)]
    (gmail/search email token query)))

(defhandler api-message [id req]
  (let [[email token] (params-for req)]
    (gmail/message email token (Integer/parseInt id))))

;; Routes
;; ------

(defroutes www-routes
  (GET "/" [] www-index)
  (route/resources "/assets"))

(defroutes api-routes
  (context "/api" []
    (GET "/" [] api-stats)
    (GET "/messages" [] api-search)
    (GET "/messages/:id" [id] (partial api-message id))))

(defroutes app-routes
  (routes
    www-routes
    (wrap-json-response api-routes)
    (route/not-found "404")))

(def app
  (-> #'app-routes
    (wrap-reload)
    (wrap-stacktrace)
    (wrap-logging)
    (wrap-worker)
    (handler/site)))

