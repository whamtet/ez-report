(ns aa-macros)

(eval
  (require '[clojure.walk :as walk])

  (defn list= [a b]
    (cond
      (and (seq? a) (seq? b))
      (every? identity (map list= a b))
      :default
      (= a b)))

  (defn capitalize-word [s]
    (str (.toUpperCase (.substring s 0 1)) (.substring s 1)))

  (defn camelize [s]
    (apply str (map capitalize-word (.split (name s) "-"))))

  (defn lower-camelize [s]
    (let [[a & rest] (.split (name s) "-")]
      (keyword (apply str a (map capitalize-word rest)))))

  (defn camelize-keys [m]
    (zipmap (map lower-camelize (keys m)) (vals m)))

  (defn replace-deref [f]
    (cond
      (list= '(clojure.core/deref) f)
      `(~(symbol (str (second f) ".deref")) ~'that)
      (and (vector? f) (not (map-entry? f)) (keyword? (first f)) (not (map? (second f))))
      (vec (list* (first f) {} (rest f)))
      :default
      f))

  (defn replace-derefs [f]
    (walk/prewalk replace-deref f))
  )

(defmacro defcomponent [name args & body]
  (let [
         lifecycle-method? #(and (coll? %) (= 'fn (first %)))
         lifecycle-methods (filter lifecycle-method? body)
         body (remove lifecycle-method? body)
         ]
    `(~'class ~(with-meta (symbol (camelize name)) {:export true}) ~'UnmountingComponent
              (~'fn ~'constructor [~'props]
                    (~'super (~'+ ~(str name) ~'props.key)))
              ~@lifecycle-methods
              (~'fn ~'render []
                    (~'let [
                             ~'that ~'this
                             ~args ~'this.props.args
                             ]
                           ~@(replace-derefs body))))))

(defmacro invoke-component [namez k & args]
  (assert (not (keyword? (first args))))
  (assert (not (.contains (name namez) "/")))
  `[~(keyword (camelize namez)) {:key ~k :args ~(vec args)}])

(defmacro invoke-component2 [namez & args]
  (assert (not (keyword? (first args))))
  (assert (not (.contains (name namez) "/")))
  `[~(keyword (camelize namez)) {:key ~(str namez) :args ~(vec args)}])
