
(ns groxy.metrics
  (:require [clj-statsd :refer [gauge increment setup with-timing]]
            [fisher.core :refer [general]]))

;; Public
;; ------

(defn- configure! [config]
  (setup
    (:statsd-host config)
    (:statsd-port config)
    :prefix "groxy."))

(defn- keep-alive [config]
  (future
    (let [timeout (:statsd-timeout-ms config)]
      (while true
        (let [mem (:memory (general))]
          (gauge :memory-total (:total mem))
          (gauge :memory-free (:free mem)))
        (Thread/sleep timeout)))))

;; Public
;; ------

(defn wrap-metrics [handler]
  (fn [req]
    (increment
      :request-count)
    (with-timing
      :request-time
      (handler req))))

(defn init [config]
  (configure! config)
  (keep-alive config))

