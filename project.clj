
(defproject groxy "0.4.0"
  :description "Gmail OAuth proxy API"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [compojure "1.1.6"]
                 [enlive "1.1.5"]
                 [rodnaph/confo "0.7.0"]
                 [rodnaph/cail "0.4.3"]
                 [javax.mail/mail "1.4.7"]
                 [boxuk/wrap-worker "0.2.0"]
                 [rodnaph/fisher "0.2.0"]
                 [cheshire "5.3.1"]
                 [ring/ring-jetty-adapter "1.2.1"]
                 [clj-logging-config "1.9.10"]
                 [org.clojure/tools.logging "0.2.6"]
                 [org.clojure/core.cache "0.6.3"]
                 [org.clojure/java.jdbc "0.3.2"]
                 [mysql/mysql-connector-java "5.1.28"]
                 [clj-statsd "0.3.10"]]
  :plugins [[lein-marginalia "0.7.1"]
            [lein-ring "0.8.10"]
            [lein-bin "0.3.4"]]
  :source-paths ["src/clojure" "resources/views"]
  :java-source-paths ["src/java"]
  :ring {:handler groxy.web/app}
  :bin {:name "groxy"}
  :jvm-opts ["-Xmx128m"]
  :main groxy.core)

