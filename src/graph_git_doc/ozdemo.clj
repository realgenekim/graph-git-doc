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
           (assoc datemap :lines x))
         l2)))


(defn extract-change-ranges [commits]
  (->> commits
       (map extract-change-range)
       flatten))


#_ (extract-change-range x1)


(defn gen-strip-plot []
  {:width    600
   :data     {:name   "table",
              :values (extract-change-ranges commits-with-changes)}
   :mark     "tick",
   :encoding {
              :x {:field "date", :type "temporal"},
              :y {:field "lines", :type "quantitative"}}})


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



;
;
;
;

"

"

(defn group-data [& names]
  (apply concat (for [n names]
                  (map-indexed (fn [i x]
                                 {:x i :y x :col n})
                               (take 20 (repeatedly #(rand-int 100)))))))

(def line-plot
  {:width    600
   :data     {:values (group-data "monkey" "slipper" "broom")}
   :encoding {:x     {:field "x"}
              :y     {:field "y"}
              :color {:field "col" :type "nominal"}}
   :mark     "line"})

#_(oz/v! line-plot)

(def jsdata "{\"data\": [\n    {\n      \"name\": \"table\",\n      \"values\": [\n        {\"category\": \"A\", \"amount\": 28},\n        {\"category\": \"B\", \"amount\": 55},\n        {\"category\": \"C\", \"amount\": 43},\n        {\"category\": \"D\", \"amount\": 91},\n        {\"category\": \"E\", \"amount\": 81},\n        {\"category\": \"F\", \"amount\": 53},\n        {\"category\": \"G\", \"amount\": 19},\n        {\"category\": \"H\", \"amount\": 87}\n      ]\n    }\n  ],\n}")

(def bar-chart
  {:width    600
   :data     {:name   "table",
              :values [{:category "A", :amount 28}
                       {:category "B", :amount 55}
                       {:category "C", :amount 43}
                       {:category "D", :amount 91}
                       {:category "E", :amount 81}
                       {:category "F", :amount 53}
                       {:category "G", :amount 19}
                       {:category "H", :amount 87}]}
   :mark     "bar",
   :encoding {
              :x {:field "category", :type "ordinal"},
              :y {:field "amount", :type "quantitative"}}})

#_(oz/v! bar-chart)



;(comment
;  {
;   "$schema": "https://vega.github.io/schema/vega-lite/v3.json",
;            "data": {"url": "data/movies.json"},
;   "mark": "bar",
;            "encoding": {
;                                   "x": {
;                                                              "bin": true,
;                                                                   "field": "IMDB_Rating",
;                                                              "type": "quantitative"}
;                                        ,
;                                      "y": {
;                                                                    "aggregate": "count",
;                                                                               "type": "quantitative"}}})


(def points (take 1000 (repeatedly #(rand-int 100))))

(defn hpoints []
  (map (fn [x] {:amount x}) points))


(def histogram
  {:width    600
   :data     {:name   "table",
              :values (hpoints)}
   :mark     "bar",
   :encoding {
              :x {:bin   true
                  :field "amount", :type "quantitative"},
              :y {:aggregate "count", :type "quantitative"}}})

#_(oz/v! histogram)

;(comment
;  {
;   "$schema": "https://vega.github.io/schema/vega-lite/v3.json",
;            "description": "A vertical 2D box plot showing median, min, and max in the US population distribution of age groups in 2000.",
;   "data": {"url": "data/population.json"},
;            "mark": {
;                               "type": "boxplot",
;                                     "extent": 1.5}
;                    ,
;   "encoding": {
;                 "x": {"field": "age","type": "ordinal"},
;                    "y": {
;                                         "field": "people",
;                                                "type": "quantitative",
;                                         "axis": {"title": "population"}}
;                         ,
;                 "size": {"value": 5}}})

(defn bpoints [n]
  (take n (repeatedly #(into {} {:x (rand-int 10)
                                 :y (rand-int 100)}))))

;
; Fantastic talk at ClojureConj/2018!  I loved the work you're doing around
; Polis, and was delighted to learn that you are the author of oz!
;
; I'm getting an error, despite the fact that it matches the vega-lite demo here:
; https://vega.github.io/editor/#/examples/vega-lite/boxplot_minmax_2D_vertical
;
; error in browser console is:
;
;:dependencies [[org.clojure/clojure "1.10.0-RC2"]
;                 [org.clojure/tools.cli "0.3.5"]
;                 [org.clojure/data.csv "0.1.4"]
;                 [org.clojure/tools.cli "0.3.7"]
;                 [clj-http "3.9.1"]
;                 ; for type checking
;                 [gnl/ghostwheel "0.2.3"]
;                 ; generative testing
;                 [org.clojure/test.check "0.9.0"]
;                 ; graphing/stats
;                 [incanter "1.5.7"]
;                 [cljsjs/vega "4.4.0-0"]
;                 [cljsjs/vega-lite "3.0.0-rc10-0"]
;                 [cljsjs/vega-embed "3.24.1-0"]
;                 [cljsjs/vega-tooltip "0.13.0-0"]
;                 [metasoarous/oz "1.3.1"]
;                 [com.cognitect/transit-clj "0.8.313"]
;                 [org.clojure/data.json "0.2.6"]]

"Uncaught Error: Unregistered composite mark boxplot
   at Object.n.normalize (oz.js:14)
   at B (oz.js:14)
   at Object.q [as normalize] (oz.js:14)
   at Object.n.compile (oz.js:14)
   at AI (oz.js:1213)
   at Object.<anonymous> (oz.js:1214)
   at Object.<anonymous> (oz.js:608)
   at Object.componentDidMount (oz.js:29)
   at e.notifyAll (oz.js:42)
   at r.close (oz.js:44)"

;
;


(def boxplot
  {:width    400
   :data     {:name   "table"
              :values (bpoints 10)}
   :mark     {:type   "boxplot"
              :extent 1.5}
   :encoding {
              :x {:field "x", :type "ordinal"
                  :axis  {:title "ABC"}},
              :y {:field "y", :type "quantitative"}}})


#_(oz/v! boxplot)

;(comment
;  {
;   "$schema": "https://vega.github.io/schema/vega-lite/v3.json",
;            "description": "A scatterplot showing horsepower and miles per gallons for various cars.",
;   "data": {"url": "data/cars.json"},
;            "mark": "point",
;   "encoding": {
;                 "x": {"field": "Horsepower","type": "quantitative"},
;                    "y": {"field": "Miles_per_Gallon","type": "quantitative"}}})

(defn fx [v]
  (let [jitter (- (rand-int 10) 5)]
    (+ v jitter)))

(defn scatter-points [n]
  (take n (repeatedly
            (fn []
              (let [x (rand-int 100)
                    y (fx x)]
                (into {} {:x x
                          :y y}))))))

(def scatterplot
  {:width    600
   :data     {:name   "table"
              :values (scatter-points 100)}
   ;:values [{:age 1 :y 5} {:age 1 :y 10}]}
   :mark     {:type "point"}
   :encoding {
              :x {:field "x", :type "quantitative"},
              :y {:field "y", :type "quantitative"}}})

#_(oz/v! scatterplot)

(def scatterplot-regression
  {:width 600
   ;:values [{:age 1 :y 5} {:age 1 :y 10}]}
   :layer [
           {:data     {:name   "table"
                       :values (scatter-points 100)}

            :mark     {:type "point"}
            :encoding {
                       :x {:field "x", :type "quantitative"},
                       :y {:field "y", :type "quantitative"}}}
           {:data     {:name   "line"
                       :values [{:x 0 :y 0}
                                {:x 100 :y 100}]}
            :mark     {:type "line"}
            :encoding {
                       :x     {:field "x", :type "quantitative"},
                       :y     {:field "y", :type "quantitative"}
                       :color {:value "firebrick"}}}]})


#_(oz/v! scatterplot-regression)

(oz/view! [:div
           [:h1 "Look ye and behold"]
           [:p "A couple of small charts"]
           [:div {:style {:display "flex" :flex-direction "row"}}
            [:vega-lite line-plot]]
           [:div {:style {:display "flex" :flex-direction "row"}}
            [:vega-lite line-plot]]])

(defn render []
  (oz/start-plot-server!)
  (oz/view! [:div
             [:h1 "Look ye and behold"]
             [:p "A couple of small charts"]
             [:div {:style {:display "flex" :flex-direction "row"}}
              [:vega-lite line-plot]]
             [:div {:style {:display "flex" :flex-direction "row"}}
              [:vega-lite bar-chart]]
             [:div {:style {:display "flex" :flex-direction "row"}}
              [:vega-lite histogram]]
             [:div {:style {:display "flex" :flex-direction "row"}}
              [:vega-lite boxplot]]
             [:div {:style {:display "flex" :flex-direction "row"}}
              [:vega-lite scatterplot-regression]]]))

