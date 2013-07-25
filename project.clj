
(defproject groxy "0.4.0"
  :description "Gmail OAuth proxy API"
  :dependencies [[org.clojure/clojure "1.5.0"]
                 [compojure "1.1.5"]
                 [enlive "1.0.1"]
                 [rodnaph/confo "0.7.0"]
                 [rodnaph/cail "0.4.1"]
                 [javax.mail/mail "1.4.7"]
                 [boxuk/wrap-worker "0.2.0"]
                 [rodnaph/fisher "0.2.0"]
                 [cheshire "5.1.0"]
                 [ring/ring-jetty-adapter "1.1.8"]
                 [clj-logging-config "1.9.10"]
                 [org.clojure/tools.logging "0.2.6"]
                 [org.clojure/core.cache "0.6.3"]]
  :plugins [[lein-marginalia "0.7.1"]
            [lein-ring "0.8.3"]
            [lein-bin "0.3.2"]]
  :source-paths ["src/clojure" "resources/views"]
  :java-source-paths ["src/java"]
  :ring {:handler groxy.web/app}
  :bin {:name "groxy"}
  :jvm-opts ["-Xmx64m"]
  :main groxy.core)

