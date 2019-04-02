(ns graph-git-doc.ozdemo
  (:require
    [oz.core :as oz]
    [clojure.data.json :as json]
    [graph-git-doc.manuscript-lines :as c]
    [graph-git-doc.git-log-text :as log]
    [graph-git-doc.wordcountcsv :as wc]))


(oz/start-plot-server!)


(defn extract-data [commits]
  (->> (map (fn [c]
              {:date  (.format (java.text.SimpleDateFormat. "MM/dd/yy")
                               (:date c))
               :lines (:lines c)})
            commits)
       (remove #(nil? (:lines %)))))

#_ (extract-data commits)

(defn gen-graph [commits]
  {:width    600
   :data     {:name   "table",
              :values (extract-data commits)}
   :mark     "bar",
   :encoding {
              :x {:field "date", :type "temporal"},
              :y {:field "lines", :type "quantitative"}}})

(defn gen-graph-simple [commits]
  {:width    600
   :data     {:name   "table",
              :values [{:date "09/08/18", :lines 3291}
                       {:date "03/07/18", :lines 3278}
                       {:date "07/18/18", :lines 3303}
                       {:date "07/17/18", :lines 3306}
                       {:date "07/17/18", :lines 3438}
                       {:date "07/17/18", :lines 3438}
                       {:date "07/17/18", :lines 3202}
                       {:date "05/23/17", :lines nil}]}
   :mark     "bar",
   :encoding {
              :x {:field "date", :type "temporal"},
              :y {:field "lines", :type "quantitative"}}})


(defn graph-word-count []
  (let [commits (c/get-commit-with-lines!)]
    (gen-graph-simple commits)))


(comment
  (oz/start-plot-server!)
  (oz/v!)
  (oz/v! (graph-word-count))

  (def commits graph-git-doc.manuscript-lines/get-commit-with-lines!)
  (oz/v! (gen-graph-simple commits)))


#_ (oz/v! (gen-graph commits))
#_ (oz/v! (gen-graph-simple commits))
#_ (oz/start-plot-server!)

;;;;;  strip-plot of changes

; strip plot: https://vega.github.io/vega-lite/examples/tick_strip.html

(defn gen-strip-plot-simple []
  {:width    600
   :data     {:name   "table",
              :values [{:date "09/08/18", :lines 3291}
                       {:date "03/07/18", :lines 3278}
                       {:date "07/18/18", :lines 3303}
                       {:date "07/17/18", :lines 3306}
                       {:date "07/17/18", :lines 3438}
                       {:date "07/17/18", :lines 3438}
                       {:date "07/17/18", :lines 3202}
                       {:date "05/23/17", :lines nil}]}
   :mark     "tick",
   :encoding {
              :x {:field "date", :type "temporal"},
              :y {:field "lines", :type "quantitative"}}})

#_ (oz/v! (gen-strip-plot-simple))

(defn gen-date [c]
  {:date  (.format (java.text.SimpleDateFormat. "MM/dd/yy")
                   (:date c))})



;(def x1 (first commits-with-changes))

(defn ranges [c]
  (let [[starts counts]  [(map :start-line2 (:changes c))
                          (map :count2 (:changes c))]
        ; (starts, ends) ...
        rs (map list starts counts)]
    (flatten (for [[s c] rs]
               (range s (+ s c 1))))))



(defn extract-change-range [c]
  (let [lines  (ranges c)
        l      (:lines c)
        l2     (if l
                 (conj lines l)
                 lines)
        datemap (gen-date c)]
    (map (fn [x]
           (let [inverted-line (- l x)
                 inverted-line (if (neg? inverted-line)
                                 0
                                 inverted-line)]
             (assoc datemap :lines inverted-line)))
         l2)))



(defn extract-change-ranges [commits]
  (->> commits
       (map extract-change-range)
       flatten))
       ;(map (fn [x] (invert-line-num commits x)))))


#_ (extract-change-range x1)


(defn gen-strip-plot [commits-with-changes]
  {:width    600
   :data     {:name   "table",
              :values (extract-change-ranges commits-with-changes)}
   :mark     {:type "tick"
              :opacity 0.8}
   :encoding {
              :x {:field "date", :type "temporal"},
              :y {:field "lines", :type "quantitative"
                  :axis {:title "line num changed"}}}})

(def hash-to-changes (log/gen-commit-hash-to-all-diffs))


#_(:commit (first commits))
#_(get hash-to-changes (:commit (first commits)))

(defn merge-hash-changes [c]
  (assoc c :changes
           (:changes (get hash-to-changes (:commit c)))))



(defn get-merged-changes! []
  (let [commits (graph-git-doc.manuscript-lines/get-commit-with-lines!)]
    (map merge-hash-changes commits)))

(defn graph-strip-plot []
  (let [commits-with-changes (get-merged-changes!)]
    (gen-strip-plot commits-with-changes)))

(comment
  (def commits graph-git-doc.manuscript-lines/get-commit-with-lines!))

#_ (def commits-with-changes (get-merged-changes!))


#_ (first commits-with-changes)
#_ (extract-change-ranges commits-with-changes)
#_ (extract-change-range (first commits-with-changes))

; works!


#_ (oz/v! (graph-strip-plot))

;
; word count
;
#_ (def wc (wc/read-csv))

(defn extract-wc-data [wc]
  (->> (map (fn [c]
              {:date  (:date c)
               :wordcount (:wordcount c)})
            wc)))

(defn extract-data [commits]
  (->> (map (fn [c]
              {:date  (.format (java.text.SimpleDateFormat. "MM/dd/yy")
                               (:date c))
               :lines (:lines c)})
            commits)
       (remove #(nil? (:lines %)))))

#_ (xform-wc-row (first wc))

(defn wc-line-plot [wc]
  {:width    600
   :data     {:values (extract-wc-data wc)}
   :encoding {
              :x {:field "date", :type "temporal"},
              :y {:field "wordcount", :type "quantitative"}
              :color {:value "firebrick"}}
   :mark     "line"})

(defn graph-wc-line-plot []
  (let [wc (wc/read-csv)]
    (wc-line-plot wc)))

;
;
; composite
;

(defn composite-graph []
  {:layer [
           (graph-strip-plot)
           (graph-wc-line-plot)]
   :resolve {:scale {:y "independent"}}})


(oz/v! (composite-graph))


