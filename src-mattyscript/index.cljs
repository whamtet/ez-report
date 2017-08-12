(ns index)
(literal
  "import preact from \"preact\";
  const {h} = preact;")

(import "./util" [inc dec assoc assoc-in dissoc-in modal mapcat get-in mean concat merge update-in
                  str-contains last butlast select-keys del-i])
(import "./state" [Atom SessionAtom UnmountingComponent])

(def state (Atom. []))
(set! window.state state)
(def scale (SessionAtom. "scale" {:start "A"}))
(def new-students (Atom. "Matthew whamtet@gmail.com\nFreddy Frog freddy@frog.com"))
(def current-student (Atom. 0))
;(def student-modal-tab (Atom. :schema))
;(def schema-text (SessionAtom. "schema-text" window.template-str true))

(defn add-students []
  (let [
         student-text new-students.state
         student-data (for [line (.split (.trim student-text) "\n")]
                        (let [
                               words (.split (line.trim) #"\s+")
                               [name email]
                               (if (str-contains (last words) "@")
                                 [(.join (butlast words) " ") (last words)]
                                 [(words.join " ") ""])
                               ]
                          {:name name :email email}))
         ]
    (.modal ($ "#student-modal") "hide")
    (new-students.reset "")
    (state.swap concat student-data)))

(defcomponent add-student-tab []
  [:div {}
   (.map
     @state
     (fn [{:keys [name email]} i]
       [:div {}
        [:input {:type "text"
                 :value name
                 :placeholder "Name"
                 :onchange #(state.swap assoc-in [i "name"] (-> % .-target .-value))}]
        " "
        [:input {:type "email"
                 :value email
                 :placeholder "Email"
                 :onchange #(state.swap assoc-in [i "name"] (-> % .-target .-value))}]
        " "
        [:button {:class "btn btn-default"
                  :onclick #(when (confirm (+ "Delete " name "?"))
                              (state.swap del-i i))} "Delete"]
        ]))
   [:h4 {} "New Students"]
   [:textarea {:style "width: 100%"
               :value @new-students
               :onchange
               (fn [e]
                 (set! new-students.state e.target.value))
               :rows 4}]
   [:button {:class "btn btn-default"
             :onclick add-students}
    "Add Students"]])

;(defn set-schema [])

#_(defcomponent schema-tab []
    (console.log "ss" @schema-text)
    [:div {}
     [:h4 {} "Marking Scheme"]
     [:textarea {:style "width: 100%"
                 :value @schema-text
                 :onchange
                 (fn [e]
                   (set! schema-text.state e.target.value))
                 :rows 4}]
     [:button {:class "btn btn-default"
               :onclick set-schema}
      "Set Schema"]])

(defcomponent student-modal []
  (let [
         ;student-modal-tab-val @student-modal-tab
         ]
    (modal "student-modal" "Students" ;(if (= :student student-modal-tab-val) "Students" "Schema")
           [:div {}
            #_[:ul {:class "nav nav-tabs"}
               [:li {:class "nav-item"
                     :onclick #(student-modal-tab.reset :student)}
                [:a {:href "#"
                     :style "padding: 7px"
                     :class (if (= :student student-modal-tab-val)
                              "nav-link active" "nav-link")} "Students"]]
               [:li {:class "nav-item"
                     :onclick #(student-modal-tab.reset :schema)}
                [:a {:href "#"
                     :style "padding: 7px"
                     :class (if (= :schema student-modal-tab-val)
                              "nav-link active" "nav-link")} "Scheme"]]]
            (invoke-component2 add-student-tab)])))

(defn set-scale-start [start]
  (scale.swap
    merge
    (cond
      (= "A" start) {:start "A"}
      (= "A+" start) {:start "A+"}
      (= "1" start) {:start "1" :end "10"})))

(defn set-scale-end [end]
  (scale.swap assoc :end end))

(defn scaling [s]
  (let [
         {:keys [start end even-weighting]} s
         ]
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
         (for [value ["10" "100"]]
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
               {scale-start :start scale-end :end}
               student-val]
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
                [:strong {:onclick #(state.swap dissoc-in [student-val i phrase])} phrase "; "]
                [:span {:onclick #(state.swap assoc-in [student-val i phrase] true)} phrase "; "]))])])]]))

(defcomponent app []
  (fn componentDidMount []
    (.modal ($ "#student-modal") "show"))
  (let [
         current-student-val @current-student
         state-val @state
         edit-button [:button {:class "btn btn-default"
                               :onclick #(.modal ($ "#student-modal") "show")} "Edit"]
         ]
    (if (get state-val current-student-val)
      (let [
             {:keys [name]} (get state-val current-student-val)
             scale-val @scale
             scores (window.template.map
                      (fn [{:keys [comments]} i]
                        (let [
                               active-comments (get-in state-val [current-student-val i] {})
                               ]
                          (section-scores active-comments comments))))
             mean-score (if (get scale-val :even-weighting)
                          (mean (.filter (scores.map mean) isFinite))
                          (mean (apply concat scores)))
             overall-score (average-score mean-score scale-val.start scale-val.end)
             ]
        [:div {:class "container"}
         (invoke-component2 student-modal)
         [:h2 {} "Report for " name]
         (scaling scale-val)
         [:br {}]
         (window.template.map #(section %1 %2 (get-in state-val [current-student-val %2] {}) scale-val current-student-val))
         [:br {}]
         [:h4 {} "Overall Score: " overall-score]
         [:button {:class "btn btn-default"
                   :onclick
                   #(when (confirm "Clear Scores?")
                      (state.swap update-in [current-student-val] select-keys ["name" "email"]))} "Clear"]
         " "
         (if (> current-student-val 0)
           [:button {:class "btn btn-default"
                     :onclick #(current-student.swap dec)} "Previous Student"])
         " "
         (if (< current-student-val (dec state-val.length))
           [:button {:class "btn btn-default"
                     :onclick #(current-student.swap inc)} "Next Student"])
         " "
         [:button {:class "btn btn-default"
                   :data-toggle "modal"
                   :data-target "#finalize"} "Complete"]
         " "
         edit-button
         ])
      [:div {:class "container"}
       (invoke-component2 student-modal)
       [:h3 {} "No Students Selected"]
       edit-button])))

(defn main []
  (let [container (document.getElementById "application")]
    (preact.render [:App {:key "app"}] container container.lastElementChild)))

;start the app

(set! window.main main)
