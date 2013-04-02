
(defproject groxy "0.0.1"
  :description "Gmail OAuth proxy API"
  :dependencies [[org.clojure/clojure "1.5.0"]
                 [compojure "1.1.5"]
                 [http-kit "2.0.0-RC4"]
                 [ring/ring-devel "1.1.8"]
                 [enlive "1.0.1"]
                 [confo "0.1.1"]
                 [javax.mail/mail "1.4.5"]
                 [cheshire "5.0.2"]]
  :source-paths ["src/clojure" "resources/views"]
  :java-source-paths ["src/java"]
  :main groxy.core)

