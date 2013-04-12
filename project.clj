
(defproject groxy "0.1.1"
  :description "Gmail OAuth proxy API"
  :dependencies [[org.clojure/clojure "1.5.0"]
                 [compojure "1.1.5"]
                 [http-kit "2.0.0"]
                 [ring/ring-devel "1.1.8"]
                 [enlive "1.0.1"]
                 [confo "0.1.1"]
                 [javax.mail/mail "1.4.7"]
                 [cheshire "5.0.2"]
                 [wrap-worker "0.1.0"]]
  :plugins [[lein-marginalia "0.7.1"]
            [lein-ring "0.8.3"]]
  :source-paths ["src/clojure" "resources/views"]
  :java-source-paths ["src/java"]
  :ring {:handler groxy.web/app}
  :main groxy.core)

