(defproject ez-report "1.0.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://ez-report.herokuapp.com"
  :license {:name "FIXME: choose"
            :url "http://example.com/FIXME"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [compojure "1.1.8"]
                 [ring/ring-jetty-adapter "1.2.2"]
                 [ring/ring-devel "1.2.2"]
                 [ring-basic-authentication "1.0.5"]
                 [environ "0.5.0"]
                 ;[dk.ative/docjure "1.11.0"]
                 [clj-excel "0.0.1"]
                 [hiccup "1.0.5"]
                 [org.clojure/data.json "0.2.6"]]
  :min-lein-version "2.0.0"
  :plugins [[environ/environ.lein "0.2.1"]]
  :hooks [environ.leiningen.hooks]
  :uberjar-name "ez-report-standalone.jar"
  :profiles {:production {:env {:production true}}})
