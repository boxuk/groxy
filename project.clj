
(defproject groxy "0.2.0"
  :description "Gmail OAuth proxy API"
  :dependencies [[org.clojure/clojure "1.5.0"]
                 [compojure "1.1.5"]
                 [http-kit "2.0.0"]
                 [ring/ring-devel "1.1.8"]
                 [enlive "1.0.1"]
                 [confo "0.1.1"]
                 [javax.mail/mail "1.4.7"]
                 [wrap-worker "0.1.0"]
                 [fisher "0.1.0"]
                 [ring-middleware-format "0.3.0"]
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
  :jvm-opts ["-Xmx32m"]
  :main groxy.core)

