(ns graph-git-doc.oz2
  (:require
    [oz.core :as oz]
    [clojure.data.json :as json]
    [graph-git-doc.manuscript-lines :as c]
    [graph-git-doc.git-log-text :as log]
    [graph-git-doc.oz2-calc :as oz2c]
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
; strip plots
;

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

;
; use strip plot to approximate historyflow graphs
;



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
  (gen-strip-plot ops/commits)
  (gen-strip-plot (->> ops/commits
                    (take 5)))
  (gen-strip-plot (->> ops/commits
                       (drop-last 1)))
  (map :changes ops/commits)
  (map :change-ops ops/commits)
  (map :change-ops ops/commits-sample)
  (->> ops/commits
       (take 4)
       gen-strip-plot
       oz/v!)
  (->> ops/commits
       (take 4)
       (map :change-ops))
       ;gen-strip-plot)
       ;oz/v!)
  (tap> (gen-strip-plot ops/commits))
  (oz/v! (gen-strip-plot ops/commits-sample))
  (oz/v! (gen-strip-plot ops/commits))
  (oz/v! (gen-strip-plot (->> ops/commits
                           (drop-last 1))))
  (oz/v! (gen-strip-plot (->> ops/commits
                           (take 5))))
  (oz/v! (gen-strip-plot (->> ops/commits
                           (take 20))))
  (oz/v! (gen-strip-plot (->> ops/commits)))

  (->> ops/commits
    (map #(select-keys % [:date :change-ops])))
  (tap> *1)

  ,)

; get multiple series strip plot going



(comment
  (oz/start-plot-server!)
  (oz/v! multi-data)
  (println (json/write-str multi-data))
  ,)




;
;
; composite
;

(defn composite-graph []
  {:layer [
           (oz2c/gen-strip-plot ops/commits)
           (graph-wc-line-plot ops/commits)]
   :resolve {:scale {:y "independent"}}})


(comment
  (oz/v! (composite-graph)))



