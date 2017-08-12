(ns index)
(literal
  "import preact from \"preact\";
  const {h} = preact;")

(import "./util" [inc assoc assoc-in dissoc-in modal mapcat get-in mean concat merge update-in])
(import "./state" [Atom SessionAtom UnmountingComponent])

(def state (Atom. []))
(def scale (SessionAtom. "scale" {:start "A"}))
(set! window.scale scale)

(defn set-scale-start [start]
  (scale.swap
    merge
    (cond
      (= "A" start) {:start "A"}
      (= "A+" start) {:start "A+"}
      (= "1" start) {:start "1" :end "10"})))

(defn set-scale-end [end]
  (set! scale.state.end end)
  (scale.update))

(defcomponent scaling [s]
  (let [
         {:keys [start end even-weighting]} s
         select-end (cond
                      (= "A" start) ["C" "D" "E" "F"]
                      (= "1" start) ["10" "100"])
         ]
    (console.log "even-weighting" even-weighting)
    [:div {}
     [:h5 {} "Marking Scale"]
     [:select {:value start
               :onchange #(set-scale-start (-> % .-target .-value))}
      [:option {:value "A"} "A - F"]
      [:option {:value "A+"} "A+ - F-"]
      [:option {:value 1} 1]]
     (if (= "1" start)
       [" to "
         [:select {:value end
                   :onchange #(set-scale-end (-> % .-target .-value))}
          (for [value select-end]
            [:option {:value value} value])]])
     [:div {:style "margin-top: 10px"}
      [:input {:type "checkbox"
               :checked even-weighting
               :onClick (fn [e]
                          (scale.swap update-in ["even-weighting"] #(not %)))
               }] " Even Weighting"]]))

(def max-col
  (apply Math.max
         (for [sec window.template] sec.comments.length)))

(def letter-grades ["A" "B" "C" "D" "E" "F"])
(def fine-letter-grades ["A+" "A" "A-" "B+" "B" "B-" "C+" "C" "C-"
                         "D+" "D" "D-" "E+" "E" "E-" "F+" "F" "F-"])

(defn section-scores [active-comments comments]
  (mapcat
    (fn [col j]
      (for [paragraph col
            phrase (paragraph.split "; ")
            :when (get active-comments phrase)] j))
    comments))

(defn average-score [mean-score scale-start scale-end]
  (cond
    (isNaN mean-score)
    "NA"
    (= "A" scale-start)
    (get letter-grades (Math.round mean-score))
    (= "A+" scale-start)
    (get fine-letter-grades (Math.round (* 3 mean-score)))
    :default
    (let [
           average-score (- max-col mean-score)
           final-score (* (/ (Number scale-end) max-col) average-score)
           ]
      (Math.round final-score))))

(defn section [{:keys [title comments]}
               i
               active-comments
               {scale-start :start scale-end :end}]
  (let [
         scores (section-scores (or active-comments {}) comments)
         av-score (average-score (mean scores) scale-start scale-end)
         ]
    [:div {}
     [:h4 {} title]
     [:h6 {} "Section Score: " av-score]
     [:div {:class "row"}
      (for [col comments]
        [:div {:class "col-md-4" :style "border: 1px solid black; padding: 5px; margin: 5px;"}
         (for [paragraph col]
           [:p {}
            (for [phrase (paragraph.split "; ")]
              (if (get (or active-comments {}) phrase)
                [:strong {:onclick #(state.swap dissoc-in [i phrase])} phrase "; "]
                [:span {:onclick #(state.swap assoc-in [i phrase] true)} phrase "; "]))])])]]))

(defcomponent app []
  (let [
         state-val @state
         scale-val @scale
         scores (window.template.map
                  (fn [{:keys [comments]} i]
                    (let [
                           active-comments (get state-val i {})
                           ]
                      (section-scores active-comments comments))))
         mean-score (if (get scale-val :even-weighting)
                      (mean (.filter (scores.map mean) isFinite))
                      (mean (apply concat scores)))
         overall-score (average-score mean-score scale-val.start scale-val.end)
         ]
    [:div {:class "container"}
     [:h2 {} "Report"]
     (invoke-component2 scaling scale-val)
     [:br {}]
     (window.template.map #(section %1 %2 (get state-val %2 {}) scale-val))
     [:br {}]
     [:h4 {} "Overall Score: " overall-score]
     [:button {:class "btn btn-default"
               :onclick #(state.reset [])} "Clear"]
     " "
     [:button {:class "btn btn-default"
               :data-toggle "modal"
               :data-target "#finalize"} "Complete"]]))

(defn main []
  (let [container (document.getElementById "application")]
    (preact.render [:App {:key "app"}] container container.lastElementChild)))

;start the app

(set! window.main main)
