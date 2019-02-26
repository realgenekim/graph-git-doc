(ns graph-git-doc.wordcountcsv
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [java-time :as time]))

(defn csv-data->maps
  " turn csv into map: from csv library "
  [csv-data]
  (map zipmap
       (->> (first csv-data)                                ;; First row is the header
            ;(map (comp keyword str/lower-case))             ;; Drop if you want string keys instead
            (map keyword)                                   ;; Drop if you want string keys instead
            repeat)
       (rest csv-data)))

(defn read-csvfile-to-map!
  " given filename, return list of maps "
  [infile]
  (with-open [reader (io/reader infile)]
    (let [rows (->>
                 ; read all rows, not lazy
                 (doall (csv/read-csv reader))
                 ; convert to map
                 csv-data->maps)]
      rows)))

(defn xform-row
  " convert dates, and wordcount string->int"
  [row]
  {
   ;:date (time/in (time/local-date "MM/dd/yy" (:date row)))
   ;:date (.toInstant (.atZone (.atStartOfDay
   ;                             (time/local-date "MM/dd/yy" (:date row)))
   ;                           (time/zone-id)))
   :date (:date row)
   :wordcount (Integer/parseInt (:wordcount row))})


(defn read-csv []
  (let [rows     (read-csvfile-to-map! "wordcount.csv")]
    (map xform-row rows)))
