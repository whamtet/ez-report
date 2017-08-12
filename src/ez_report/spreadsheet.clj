(ns ez-report.spreadsheet)

;(require '[dk.ative.docjure.spreadsheet :as spreadsheet])
(use 'clj-excel.core)
(import java.io.File)

(defn safe-nth [s i]
  (if (< i (count s)) (nth s i)))

(defn transpose
  "transposes without the nils"
  [s]
  (let [
         n (apply max (map count s))
         ]
    (for [i (range n)
          :let [x (filter identity (map #(safe-nth % i) s))]
          :when (not-empty x)]
      x)))

(defn parse-workbook [f]
  (let [
         book (lazy-workbook (workbook-hssf f))
         first-sheet-name (first (sort (keys book)))
         first-sheet (book first-sheet-name)
         sections (take-nth 2 (partition-by #(= "***" (first %)) first-sheet))
         ]
    (for [[[title] & comments] sections]
      {:title title
       :comments (transpose comments)})))
