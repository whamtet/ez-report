(ns ez-report.nashorn)

(import javax.script.ScriptEngineManager)
(require '[clojure.java.io :as io])
(require '[ez-report.util :as util])

(defonce engine
  (doto
    (.getEngineByName (ScriptEngineManager.) "nashorn")
    ;only want this one once
    (.eval (io/reader (io/resource "nashorn-polyfill.js")))))

(.eval engine "window.nashorn = true")
(.eval engine "location = {protocol: ''}")
(.eval engine "navigator = {userAgent: ''}")
(.eval engine "history = {}")
(.eval engine (io/reader (io/resource "public/preact.bundled.js")))

(defmacro defn-dev [s args & body]
  `(def ~s
     (if util/dev?
       (fn ~args ~@body)
       (fn ~args ~(last body)))))

(def s
  (if util/dev?
    #(when %
       (.eval engine (io/reader "resources/public/preact.bundled.js"))
       (.eval engine %))
    (memoize #(if % (.eval engine %)))))

;(defonce latest-args (atom nil))
(defn-dev call [f & args]
  (.eval engine (io/reader "resources/public/preact.bundled.js"))
  (.call (.eval engine f) nil (object-array args)))

