
(ns groxy.web
  (:use compojure.core
        ring.middleware.reload
        ring.middleware.stacktrace
        net.cgrand.enlive-html
        [wrap-worker.core :only [wrap-worker]])
  (:require (compojure [handler :as handler]
                       [route :as route])
            [groxy.gmail :as gmail]
            [groxy.stats :as stats]
            [cheshire.core :as json]))

(defmacro as-json [& body]
  `(try
     (let [body# (doall ~@body)]
       (json-response body#))
     (catch Exception e#
         (.printStackTrace e#)
         (json-response 403 (.getMessage e#)))))

(defn json-response
  ([content] (json-response 200 content))
  ([status content]
    {:status status
     :content-type "application/json"
     :body (json/generate-string content)}))

(defn params-for [req]
  (let [params (:params req) ]
    [(:email params)
     (:access_token params)
     (:query params)]))

;; WWW Pages
;; ---------

(deftemplate
  layout "index.html"
  [title]
  [:title] (content title))

(defn page-index [req]
  (layout "Home"))

;; API Handlers
;; ------------

(defn api-stats [req]
  (json-response (stats/server)))

(defn api-search [req]
  (let [[email token query] (params-for req)]
    (as-json
      (gmail/search email token query))))

(defn api-message [id req]
  (let [[email token] (params-for req)]
    (as-json
      (gmail/message email token (Integer/parseInt id)))))

;; Routes
;; ------

(defroutes app-routes
  (GET "/" [] page-index)
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
    (wrap-worker)
    (handler/site)))

