(ns state)

(import "./util" [assert assoc dissoc])
(literal
  "import preact from \"preact\";
  const { h, Component } = preact;")

(defn safe-parse [s]
  (cond (= "undefined" s) nil
        :default (JSON.parse s)))

(defn session-get [k]
  (if window.sessionStorage (some-> k window.sessionStorage.getItem safe-parse)))
(defn session-set [k v]
  (if window.sessionStorage
    (try (window.sessionStorage.setItem k (JSON.stringify v)) (catch e))))
(defn session-remove [k]
  (if window.sessionStorage (window.sessionStorage.removeItem k)))

(class ^:export UnmountingComponent Component
       (fn constructor [name]
         (super)
         (assert name "null name in UnmountingComponent constructor")
         (set! this.name name))
       (fn componentDidUnmount []
         (set! this.unmounted true)))

(class ^:export Atom
       (fn constructor [value]
         (set! this.state value)
         (set! this.components {}))
       (fn deref [component]
         (assert component.name "no name for component")
         (assoc this.components component.name component)
         this.state)
       (fn reset [new-value]
         (set! this.state new-value)
         (let [
                old-components this.components
                ]
           (set! this.components {})
           (doseq [k (Object.keys old-components)
                   :let [component (get old-components k)]
                   :when (not component.unmounted)]
             (assoc this.components k component)
             (component.setState component.state))))
       (fn update []
         (this.reset this.state))
       (fn swap [f & args]
         (this.reset (apply f this.state args))))

(class ^:export SessionAtom Atom
       (fn constructor [k value override?]
         (super)
         (let [k2 (+ location.pathname k)]
           (set! this.state (or (if-not override? (session-get k2)) value))
           (set! this.k k2)))
       (fn reset [new-value]
         (super.reset new-value)
         (session-set this.k new-value)))
