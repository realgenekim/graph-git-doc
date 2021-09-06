(ns graph-git-doc.oz2
  (:require
    [oz.core :as oz]
    [clojure.data.json :as json]
    [graph-git-doc.manuscript-lines :as c]
    [graph-git-doc.git-log-text :as log]
    [graph-git-doc.wordcountcsv :as wc]
    [graph-git-doc.ops :as ops]
    [portal.api :as p]))


;(oz/start-plot-server!)

(comment
  (p/open) ; Open a new inspector
  (add-tap #'p/submit) ; Add portal as a tap> target
  (tap> :hello)
  ,)

;
; bar graph
;


(defn extract-data [commits]
  (->> commits
       (map (fn [c]
              {:date  (:date c)
               :lines (:stats-num-words c)}))
       (remove #(nil? (:lines %)))))

(defn gen-graph-canned [commits]
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

(defn gen-graph [commits]
  {:width    600
   :data     {:name   "table",
              :values (extract-data commits)}
   :mark     "bar",
   :encoding {
              :x {:field "date", :type "temporal"},
              :y {:field "lines", :type "quantitative"}}})



(comment
  (oz/start-plot-server!)
  (tap> ops/commits)

  (oz/v! (gen-graph-canned ops/commits))

  (extract-data ops/commits)
  (gen-graph ops/commits)
  (oz/v! (gen-graph ops/commits))


  ,)

;
; line graph: word count
;

;
; word count
;
#_ (def wc (wc/read-csv))

(defn extract-wc-data [wc]
  (->> wc
       (map (fn [c]
              {:date  (:date c)
               :wordcount (:stats-num-words c)}))))

#_ (xform-wc-row (first wc))

(defn wc-line-plot [wc]
  {:width    600
   :data     {:values (extract-wc-data wc)}
   :encoding {
              :x {:field "date", :type "temporal"},
              :y {:field "wordcount", :type "quantitative"}
              :color {:value "firebrick"}}
   :mark     "line"})

(defn graph-wc-line-plot [commits]
  (wc-line-plot commits))

(comment
  (extract-wc-data ops/commits)
  (oz/v! (graph-wc-line-plot ops/commits))

  ,)

;
;
;

(comment
  (oz/start-plot-server!)
  (oz/v!)
  (oz/v! (graph-word-count))

  (def commits graph-git-doc.manuscript-lines/get-commit-with-lines!)
  (oz/v! (gen-graph-canned commits)))


#_ (oz/v! (gen-graph commits))
#_ (oz/v! (gen-graph-canned commits))
#_ (oz/start-plot-server!)

;;;;;  strip-plot of changes

; strip plot: https://vega.github.io/vega-lite/examples/tick_strip.html

(defn gen-strip-plot-canned []
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



(comment
  (oz/v! (gen-strip-plot-canned)))

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

; [{:date "09/08/18", :lines 3291}
;                       {:date "03/07/18", :lines 3278}
;                       {:date "07/18/18", :lines 3303}
;                       {:date "07/17/18", :lines 3306}

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

(defn extract-add
  [cs]
  (->> cs
       (filter #(= :add
                   (first %)))
       first))

(defn change-op
  " input: change-set and change-op (:add, :modify, :delete)
    output: [{:date :lines :optype}]
      emit all the dots between start and start+len "
  [change change-op]
  (if-not change-op
    nil
    (do
      (println "change-op: change-op: " change-op)
      (let [[op1 start len] change-op]
        (for [l (range start (+ start len))]
          {:date   (:date change)
           :lines  l
           :optype op1})))))

(defn extract-strip-plot
  " input: one commit change set
    output: {:date XXX :lines }"
  [change]
  ;(println "extract-strip-plot: change: " change)
  (let [change-ops (:change-ops change)]
    (println "extract-strip-lots: change-ops: " change-ops)
    (some->> change-ops
             (map #(change-op change %)))))

(comment
  (extract-strip-plot (first ops/commits))
  (map :change-ops ops/commits)
  ; (([:add 1 5])
  ; ([[:modify 2 2] [:add 4 4]])
  ; ([:add 9 4])
  ; ([:add 13 4])
  ; ([:delete 12 4])
  ; ([:delete 6 3])
  ; ([:delete 5 4])
  ; ([:delete 1 4])
  ; ([:delete 1 2])
  ; ([:add 1 6])
  ; ([:modify 3 4])
  ; ([[:modify 5 2] [:add 7 2]])
  ; ([[:modify 1 4] [:add 5 2]])
  ; ())
  (map extract-strip-plot ops/commits))

(defn extract-strip-plot-data
  " input: seq of commits
    output: vega-lite data"
  [commits]
  (->> commits
       (map extract-strip-plot)
       (remove nil?)
       flatten))

(comment
  (extract-strip-plot-data ops/commits))

(defn gen-strip-plot [commits]
  {:width    600
   :data     {:name   "table",
              :values (extract-strip-plot-data commits)}
   :mark     {:type "tick"
              :opacity 0.8}
   :encoding {
              :x {:field "date", :type "temporal"},
              :y {:field "lines", :type "quantitative"
                  :axis {:title "line num changed"}}}})

(comment
  (oz/start-plot-server!)
  (gen-strip-plot ops/commits)
  (oz/v! (gen-strip-plot ops/commits)))

; get multiple series strip plot going

(def multi-data
  {:width 600,
   :data {:name "table",
          :values (list
                     {:date "2021-09-04T02:48:11.000-00:00", :lines 1 :optype "add"}
                     {:date "2021-09-04T02:48:11.000-00:00", :lines 2 :optype "add"}
                     {:date "2021-09-04T02:48:11.000-00:00", :lines 3 :optype "add"}
                     {:date "2021-09-04T02:48:11.000-00:00", :lines 4 :optype "add"}
                     {:date "2021-09-04T02:48:11.000-00:00", :lines 44 :optype "delete"}
                     {:date "2021-09-04T23:08:37.000-00:00", :lines 4 :optype "add"}
                     {:date "2021-09-04T23:08:37.000-00:00", :lines 5 :optype "add"}
                     {:date "2021-09-04T23:08:37.000-00:00", :lines 6 :optype "add"}
                     {:date "2021-09-04T23:08:37.000-00:00", :lines 30 :optype "delete"}
                     {:date "2021-09-04T23:08:37.000-00:00", :lines 32 :optype "delete"},
                     {:date "2021-09-04T23:08:37.000-00:00", :lines 342 :optype "delete"})},
   :mark {:type "point", :opacity 0.8},
   :encoding {:x {:field "date", :type "temporal"
                  :timeUnit "monthdate"},
              :y {:field "lines", :type "quantitative", :axis {:title "line num changed"}}
              :color {:condition {:param "brush"
                                  :title "ops"
                                  :field "optype"
                                  :type "nominal"
                                  :scale {:domain ["add" "delete"]
                                          :range  ["blue" "red"]}}
                      :value "red"}}
   :params [{:name "brush"
             :select {:type "interval"
                      :encodings ["x"]}}]})

(comment
  (oz/start-plot-server!)
  (oz/v! multi-data)
  (println (json/write-str multi-data))
  ,)

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
;
; composite
;

(defn composite-graph []
  {:layer [
           (graph-strip-plot)
           (graph-wc-line-plot)]
   :resolve {:scale {:y "independent"}}})


;(oz/v! (composite-graph))


