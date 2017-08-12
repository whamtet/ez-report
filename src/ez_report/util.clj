(ns ez-report.util)

(def dev? (= "Mac OS X" (System/getProperty "os.name")))

(defn text-response [s]
  {:status 200
   :headers {"Content-Type" "text/plain; charset=utf-8"}
   :body s})

(defn json-response [s]
  {:status 200
   :headers {"Content-Type" "application/json"}
   :body s})

(defn html-response [s]
  {:status 200
   :headers {"Content-Type" "text/html; charset=utf-8"}
   :body s})
