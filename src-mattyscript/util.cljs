(ns util)

(defn ^:export prn [x]
  (console.log (JSON.stringify x)))

(defn ^:export get-in [m [k & ks]]
  (let [
         v (get m k)
         ]
    (if (and v (> ks.length 0))
      (get-in v ks)
      v)))

(defn ^:export seqm [m]
  (for [k (Object.keys m)]
    [k (get m k)]))

(defn ^:export merge [m1 m2]
  (let [
         out {}
         ]
    (doseq [[k v] (seqm m1)]
      (assoc out k v))
    (doseq [[k v] (seqm m2)]
      (assoc out k v))
    out))

(defn ^:export concat [& arrays]
  (arrays.reduce #(.concat %1 %2)))

(defn ^:export mapcat [f s]
  (apply concat (s.map f)))

(defn ^:export assert [b msg]
  (if-not b (throw (Error. msg))))

(defn ^:export assoc [m k v]
  (let [m2 (or m {})]
    (literal "m2[k] = v;")
    m2))

(defn ^:export assoc-in [m [k & ks] v]
  (let [
         m2 (or m {})
         ]
    (if (> ks.length 0)
      (assoc m2 k (assoc-in (get m2 k) ks v))
      (assoc m2 k v))))

(defn ^:export update-in [m [k & ks] f & args]
  (let [
         m2 (or m {})
         v (get m2 k)
         ]
    (if (= 0 ks.length)
      (assoc m2 k (apply f v args))
      (assoc m2 k (apply update-in v ks f args)))
    m2))

(defn ^:export dissoc [m k]
  (literal "delete m[k]")
  m)

(defn ^:export dissoc-in [m [k & ks]]
  (if (> ks.length 0)
    (dissoc-in (get m k) ks)
    (literal "delete m[k]"))
  m)

(defn ^:export inc [x]
  (+ x 1))

(defn ^:export sum [x]
  (x.reduce #(+ %1 %2) 0))

(defn ^:export mean [x]
  (/ (sum x) x.length))

(defn ^:export modal [id title body footer]
  [:div {:class "modal fade" :id id :tabindex "-1" :role "dialog"}
   [:div {:class "modal-dialog" :role "document"}
    [:div {:class "modal-content"}
     [:div {:class "modal-header"}
      [:button {:type "button" :class "close" :data-dismiss "modal" :aria-label "Close"} "×"]
      [:h4 {:class "modal-title"} title]]
     [:div {:class "modal-body"}
      body]
     (if footer
       [:div {:class "modal-footer"}
        footer])
     ]]])
