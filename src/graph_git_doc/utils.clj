(ns analyze.utils
  #:ghostwheel.core{:trace       true
                    :check       false
                    :num-tests   0
                    :outstrument true}
  (:require
            [clojure.string :as str]
            [clojure.pprint :as pp]
            [clojure.tools.cli :as cli]
            [clojure.java.io :as io]
            [clojure.data.csv :as csv]
            [clojure.spec.alpha :as s]
            [ghostwheel.core :as g
             :refer [>defn >defn- >fdef => | <- ?]])
  (:gen-class))

(defn print-stderr [s]
  (binding [*out* *err*]
    (println s)))

(defn pr-stderr [s]
  (binding [*out* *err*]
    (pr s)
    (.flush *err*)))

(defmacro sectime
  [expr]
  `(let [start# (. System (currentTimeMillis))
         ret# ~expr]
     (print-stderr (str "Elapsed time: " (/ (double (- (. System (currentTimeMillis)) start#)) 1000.0) " secs"))
     ret#))

;
; csv file utilities
;

(defn csv-data->maps
  " turn csv into map: from csv library "
  [csv-data]
  (map zipmap
       (->> (first csv-data)                                ;; First row is the header
            ;(map (comp keyword str/lower-case))             ;; Drop if you want string keys instead
            (map keyword)                                   ;; Drop if you want string keys instead
            repeat)
       (rest csv-data)))

;
; used for csv header generation
;
(defn strip-colon-from-column-names
  ; (":abc" ":def") -> ("abc" "def")
  [m]
  (let [colnames (->>
                   m
                   ; (":request-header-User-Agent")
                   (map #(str %),,,)
                   ; ("request-header-User-Agent")
                   (map #(apply str (rest %)),,,))]
    colnames))

(defn my-write-csv!
  " given list of maps, output csv file: one row of header/names, then data"
  [rows outfile]
  (let [; merge all rows, to get all the columns
        m         (apply merge rows)
        ; trim colon from beginning of keys
        hdrs      (strip-colon-from-column-names (keys m))
        k         (keys m)
        writerows (mapv #(map % k) rows)]

    (with-open [writer (io/writer outfile)]
      ; first write the headers, then the rows data
      (csv/write-csv writer (cons hdrs writerows)))))

; sipmle reader for csv
(defn load-csv [infile]
  (with-open [reader (io/reader infile)]
    (let [rows (->>
                 ; read all rows, lazy
                 (doall (csv/read-csv reader)))]
      rows)))

(>defn read-csvfile-to-map!
  " given filename, return list of maps "
  [infile]
  [string? => sequential?]
  (with-open [reader (io/reader infile)]
    (let [rows (->>
                 ; read all rows, not lazy
                 (doall (csv/read-csv reader))
                 ; convert to map
                 csv-data->maps)]
      rows)))

; save frequency info to file
; (["ec2-52-38-236-245.us-west-2.compute.amazonaws.com" 171]
; ["ec2-54-84-52-43.compute-1.amazonaws.com" 129]
; ["ec2-52-21-223-190.compute-1.amazonaws.com" 51]
(defn save-frequency-to-file [rows outfile]
  (with-open [writer (io/writer outfile)]
    (doseq [r rows]
      (.write writer (format "%d %s\n" (second r) (first r))))))
;
;
;

(defn read-lines
  " from infile, return list of strings "
  [infile]
  (with-open [reader (io/reader "OUT/stage3-small.txt")]
    (doall (line-seq reader))))


;

;
; edn file i/o
;

;(defn edn-writefile [outfile data]
;  (spit outfile (pr-str data)))

(defn edn-writefile [outfile data]
  ; (spit outfile (pr-str data))
  (with-open [w (clojure.java.io/writer outfile)]
    (binding [*out* w]
      (clojure.pprint/write data))))

(defn edn-readfile [infile]
  (read-string (slurp infile)))

;
; write to file
;

(>defn write-to-file
  " spit doesn't take a sequence?"
  [filename lines]
  [string? sequential? => nil?]
  (with-open [w (clojure.java.io/writer filename)]
    (doseq [line lines]
      (.write w line)
      (.newLine w))))


; string searching

(>defn str-search-and-return
       " given string 'abc def ghi', return 'def' if 'def' is found
         otherwise, return nil "
       [text vs]
       [string? vector? => (s/nilable string?)]
       ;(s/nilable string?)]
       (let [indices (map #(clojure.string/index-of text %1) vs)]
         (->> ; ["a" "b" "c"] -> ([nil "a"] [0 "b"] [3 "c"]
                (map vector indices vs)
                ; throw out nil indices
                (filter #((complement nil?) (first %)))
                ; get the first tuple, return the string
                first
                last)))


;
; namespace cleanup for repl
;

(defn ns-clean
  "Remove all internal mappings from a given name space or the current one if no parameter given."
  ([] (ns-clean *ns*))
  ([ns] (map #(ns-unmap ns %) (keys (ns-interns ns)))))