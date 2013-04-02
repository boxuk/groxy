
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
  [title body]
  [:title] (content title)
  [:.content] (substitute body))

(defn json-response [content]
  {:status 200
   :content-type "application/json"
   :body (json/generate-string content)})

(defn access-denied-response [message]
  {:status 403
   :body message})

(defn index-page [req]
  (layout "Home" ""))

(defn api-inbox [req]
  (let [params (:params req)
        email (:email params)
        token (:access_token params)]
    (try
      (json-response (gmail/inbox email token))
      (catch AuthenticationFailedException e
        (access-denied-response (.getMessage e))))))

(defn stats-page [req]
  (json-response (stats/server)))

(defroutes app-routes
  (GET "/" [] index-page)
  (context "/api" []
    (GET "/" [] stats-page)
    (GET "/inbox" [] api-inbox))
  (route/resources "/assets")
  (route/not-found "404..."))

(def app
  (-> #'app-routes
    (wrap-reload)
    (wrap-stacktrace)
    (handler/site)))

