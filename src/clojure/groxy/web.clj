
(ns groxy.web
  (:use compojure.core
        ring.middleware.reload
        ring.middleware.stacktrace
        net.cgrand.enlive-html)
  (:require (compojure [handler :as handler]
                       [route :as route])
            [groxy.gmail :as gmail]
            [groxy.stats :as stats]
            [cheshire.core :as json])
  (:import (javax.mail AuthenticationFailedException)))

(deftemplate
  layout "index.html"
  [title]
  [:title] (content title))

(defn json-response
  ([content] (json-response 200 content))
  ([status content]
    {:status status
     :content-type "application/json"
     :body (json/generate-string content)}))

(defn index-page [req]
  (layout "Home"))

(defn api-search [req]
  (let [params (:params req)
        email (:email params)
        token (:access_token params)
        query (:query params)]
    (try
      (json-response (gmail/search email token query))
      (catch AuthenticationFailedException e
        (json-response 403 (.getMessage e))))))

(defn api-stats [req]
  (json-response (stats/server)))

(defn api-message [id req]
  (json-response "Unimplemented"))

(defroutes app-routes
  (GET "/" [] index-page)
  (context "/api" []
    (GET "/" [] api-stats)
    (GET "/messages" [] api-search)
    (GET "/messages/:id" [id] (partial api-message id)))
  (route/resources "/assets")
  (route/not-found "404..."))

(def app
  (-> #'app-routes
    (wrap-reload)
    (wrap-stacktrace)
    (handler/site)))

