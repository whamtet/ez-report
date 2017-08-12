(ns ez-report.web
  (:require [compojure.core :refer [defroutes GET PUT POST DELETE ANY]]
            [compojure.handler :refer [site]]
            [compojure.route :as route]
            [clojure.java.io :as io]
            [ring.middleware.stacktrace :as trace]
            [ring.middleware.session :as session]
            [ring.middleware.session.cookie :as cookie]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.basic-authentication :as basic]
            [hiccup.page :as page]
            [environ.core :refer [env]]
            [ez-report.deps :as deps]
            [ez-report.nashorn :as nashorn]
            [ez-report.util :as util]
            [ez-report.spreadsheet :as spreadsheet]
            [clojure.data.json :as json]
            ))

(defn- template [{:keys [server-script vars json-vars client-script css javascripts internal-stylesheet bundle deps html]}]
  (let []
    (page/html5
      [:head
       [:meta {:charset "utf-8"}]
       [:meta {:http-equiv "X-UA-Compatible" :content "IE=edge"}]
       [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
       [:style "
        html, body {
        margin:0;
        padding:0;
        height:100%;
        }
        "
        internal-stylesheet
        ]
       (for [stylesheet (distinct (concat (mapcat :css deps) css))]
         [:link {:rel "stylesheet" :href stylesheet}])
       ]
      [:body
       html
       [:div {:id "application"} (some-> server-script nashorn/s)]
       ;for goddamn IE
       [:script {}
        "if (!String.prototype.startsWith) {
        String.prototype.startsWith = function(searchString, position) {
        position = position || 0;
        return this.substr(position, searchString.length) === searchString;
        };
        }
        if (!String.prototype.endsWith) {
        String.prototype.endsWith = function(searchString) {
        if (searchString.length > this.length) return false;
        return this.substr(this.length - searchString.length) === searchString;
        };
        }
        "]
       [:script (map (fn [[k v]] (format "var %s = %s;\n" (.replace (name k) "-" "_") (json/write-str v))) vars)]
       [:script (map (fn [[k v]] (format "var %s = %s;\n" (.replace (name k) "-" "_") v)) json-vars)]
       (for [javascript (distinct (concat (mapcat :js deps) javascripts))]
         [:script {:src javascript}])
       [:script {:src (or bundle "/public/preact.bundled.js")}]
       [:script client-script]
       ])))

(defroutes app
  (GET "/" []
       (util/html-response
         (template {:html
                    [:div {:class "container"}
                     [:h3 {} "Ez Reporting"]
                     [:a {:href "/public/Report Template.xlsx"} "Sample Template"]
                     [:p {} "Upload Excel Template"]
                     [:form {:method "POST" :enctype "multipart/form-data"}
                      [:p {} [:input {:type "file" :name "file" :enctype "multipart/form-data"}]]
                      [:input {:type "submit"}]]]
                    })))
  (POST "/" [file]
        (let [{:keys [size tempfile]} file]
          (if (= 0 size)
            (util/text-response "please select file")
            (util/html-response
              (template {
                          :deps [deps/saas deps/rc-rate]
                          :vars (spreadsheet/parse-workbook tempfile)
                          :client-script "main()"
                          })))))
  (route/resources "/public")
  (ANY "*" []
       (route/not-found (slurp (io/resource "404.html")))))

(defn wrap-error-page [handler]
  (fn [req]
    (try (handler req)
      (catch Exception e
        {:status 500
         :headers {"Content-Type" "text/html"}
         :body (slurp (io/resource "500.html"))}))))

(defn wrap-app [app]
  ;; TODO: heroku config:add SESSION_SECRET=$RANDOM_16_CHARS
  (let [store (cookie/cookie-store {:key (env :session-secret)})]
    (-> app
        ((if (env :production)
           wrap-error-page
           trace/wrap-stacktrace))
        (site {:session {:store store}}))))

(defn -main [& [port]]
  (let [port (Integer. (or port (env :port) 5000))]
    (jetty/run-jetty (wrap-app #'app) {:port port :join? false})))

;; For interactive development:
;; (.stop server)
;; (def server (-main))
