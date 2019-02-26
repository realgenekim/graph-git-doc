(ns graph-git-doc.ozdemo
  (:require
    [oz.core :as oz]
    [clojure.data.json :as json]
    [graph-git-doc.core :as c]
    [graph-git-doc.git-log-text :as log]
    [graph-git-doc.wordcountcsv :as wc]))


(def commits (c/get-commit-with-lines))

(defn extract-data [commits]
  (->> (map (fn [c]
              {:date  (.format (java.text.SimpleDateFormat. "MM/dd/yy")
                               (:date c))
               :lines (:lines c)})
            commits)
       (remove #(nil? (:lines %)))))

#_(extract-data commits)

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


(oz/v! (gen-graph commits))
(oz/v! (gen-graph-simple commits))
#_(oz/start-plot-server!)

(def hash-to-changes (log/gen-hash-to-changes))


#_(:commit (first commits))
#_(get hash-to-changes (:commit (first commits)))

(defn merge-hash-changes [c]
  (assoc c :changes
           (:changes (get hash-to-changes (:commit c)))))

(defn get-merged-changes []
  (map merge-hash-changes commits))

(def commits-with-changes (get-merged-changes))

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


(defn gen-date [c]
  {:date  (.format (java.text.SimpleDateFormat. "MM/dd/yy")
                   (:date c))})



(def x1 (first commits-with-changes))

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


(defn gen-strip-plot []
  {:width    600
   :data     {:name   "table",
              :values (extract-change-ranges commits-with-changes)}
   :mark     "tick",
   :encoding {
              :x {:field "date", :type "temporal"},
              :y {:field "lines", :type "quantitative"}}})



#_ (first commits-with-changes)
#_ (extract-change-ranges commits-with-changes)
#_ (extract-change-range (first commits-with-changes))

; works!


(oz/v! (gen-strip-plot))

;
; word count
;
(def wc (wc/read-csv))

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

(defn wc-line-plot []
  {:width    600
   :data     {:values (extract-wc-data wc)}
   :encoding {
              :x {:field "date", :type "temporal"},
              :y {:field "wordcount", :type "quantitative"}
              :color {:value "firebrick"}}
   :mark     "line"})

(oz/v! (wc-line-plot))

;
;
; composite
;

(defn composite-graph []
  {:layer [
           (wc-line-plot)
           (gen-strip-plot)]
   :resolve {:scale {:y "independent"}}})


(oz/v! (composite-graph))


