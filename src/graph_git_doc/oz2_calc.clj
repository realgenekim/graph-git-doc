(ns graph-git-doc.oz2-calc
  (:require
    [graph-git-doc.ops :as ops]))


(defn filler-op
  " input: n
    output: {:date :lines :optype} "
  [change n]
  {:date (:date change)
   :lines n
   :optype "none"})


(defn change-op
  " input: change-set and change-op (:add, :modify, :delete)
    output: [{:date :lines :optype}]
      emit all the dots between start and start+len "
  [change change-op]
  (if-not change-op
    nil
    (do
      (println "  change-op: change-op: " change-op)
      (let [[op1 start len] change-op]
        (for [l (range start (+ start len))]
          {:date   (:date change)
           :lines  l
           :optype (name op1)})))))

(defn extract-strip-plot
  " input: one commit change set
    output: {:date XXX :lines }"
  [change]
  (println "extract-strip-plot: change: " change)
  (if-not (seq (:change-ops change))
    nil
    (let [change-ops     (:change-ops change)
          numlines       (:stats-num-lines change)
          _              (println "extract-strip-plot: change-ops: " change-ops)
          out            (for [c change-ops]
                           (change-op change c))
          ;(some->> change-ops
          ;  (map #(change-op change %)))
          _          (println "extract-strip-plot: out: " out)
          ;_          (println "extract-strip-plot: numlines: " numlines)]
          filler         (if out
                           (clojure.set/difference
                             (set (range 0 numlines))
                             (set (map :lines out))))
          ;_              (println "extract-strip-plot: filler: " filler)
          filler-entries (map #(filler-op change %) filler)
          out2           (conj out filler-entries)]
      ;_              (println "extract-strip-plot: out2: " out2)]
      (flatten  out2))))
; no filler?
;(flatten out)))

(comment
  (set (range 1 10))
  ,)

(comment
  (extract-strip-plot (first ops/commits))
  (-> ops/commits
    (nth 1)
    (select-keys [:date :change-ops])))


(extract-strip-plot (second ops/commits))
(extract-strip-plot (nth ops/commits 2))
(extract-strip-plot (nth ops/commits 3))
(extract-strip-plot (nth ops/commits 4))
(map :change-ops ops/commits)
(map extract-strip-plot ops/commits)

(comment
  (def lines 4)
  (def in  [{:a 1 :b 11}
            {:a 2 :b 22}
            {:a 3 :b 33}
            {:a 4 :b 44}])
  (def out [{:a 3 :b 11}
            {:a 2 :b 22}
            {:a 1 :b 33}])

  (def x (->> in
           (map :a)
           (map #(- lines %))))

  (zipmap (vec (repeat (count x) :a)) x)
  (map :a in)
  (map :a in)

  ,)


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
   :mark     {:type "point" :shape "square" :filled true}
   :encoding {:x {:field "date", :type "ordinal"}
              ; https://vega.github.io/vega-lite/docs/timeunit.html
              ;:timeUnit "minutes"},
              ;:timeUnit "yearmonthdate"},
              :y {:field "lines", :type "quantitative", :axis {:title "line num changed"}}
              :color {:condition {:param "brush"
                                  :title "ops"
                                  :field "optype"
                                  :type "nominal"
                                  :scale {:domain ["add" "delete" "modify" "none"]
                                          :range  ["green" "red" "orange" "lightgray"]}}
                      :value "lightgray"}}
   :params [{:name "brush"
             :select {:type "interval"
                      :encodings ["x"]}}]})
