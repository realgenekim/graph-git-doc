(ns graph-git-doc.git-log-text
  "
  input: git-log.txt (generated by Makefile, which runs
    'git log'),

  output: parsed diffs
          {commithash {:start-line1 4, :count1 3, :start-line2 4, :count2 3}"
  (:require
    [graph-git-doc.parse-diff :as pd]
    [clojure.string :as s]))

(def INFILE "git-log.txt")

(def lines (clojure.string/split-lines (slurp INFILE)))

(def short (take 300 lines))

(defn- commit-line? [s]
  (re-find #"^commit (\w+)$" s))

(defn split-by-commits
  [lines current-set hash-list]
  (let [l (first lines)]
    (when true
      (do))
        ;(println "line: " l)
        ;(println "set:  " current-set)
        ;(println "list: " hash-list)
        ;(println "---")))
    (if (nil? l)
      ; last line, so return
      (conj hash-list current-set)
      ; otherwise...
      (if-let [retval (commit-line? l)]
        ; new commit-hash
        (recur (rest lines)
               {:hash (second retval) :lines [l]}
               (conj hash-list current-set))
        ; append to current lines
        (recur (rest lines)
               (assoc current-set
                 :lines
                 (conj (:lines current-set) l))
               hash-list)))))

(defn run-git-command!
  [dirname filename outfile]
  "(cd ../test-git-repo; git log --patch-with-stat --unified=1 manuscript.md) > git-log.txt"
  (let [cmd (format "(cd %s; git log --patch-with-stat --unified=1 %s) > %s"
              dirname filename outfile)
        _ (println "run-git-command! cmd: " cmd)
        out (clojure.java.shell/sh "/bin/bash" "-c" cmd)]
    (println out)))


(comment
  (run-git-command! "../test-git-repo" "manuscript.md" "git-log.txt")
  ,)


(defn list-of-commits
  " input: none (global lines)
    output: list of commits "
  [infile]
  (let [lines (-> (slurp infile)
                  (s/split-lines))]
    (split-by-commits lines {} nil)))


(comment
  (split-by-commits ["commit 123"
                     "abc"
                     "def"
                     "commit abc"
                     "hij"
                     "klm"] {} nil)

  (def x (split-by-commits (take 500 lines) {} nil))

  (def lines (s/split-lines (slurp "git-log-unicorn.txt")))
  (count lines)
  (->> lines
       (filter commit-line?)
       count)

  ,)

(defn parse-diff-line [s]
  " @@ -startline1,count1 +startline2,count2 @@
        https://stackoverflow.com/questions/8259851/using-git-diff-how-can-i-get-added-and-modified-lines-numbers
    returns nil when not valid "
  (let [retval (re-find #"^@@ \-(\d+),(\d+) \+(\d+),(\d+).*$" s)]
    (if retval
      {:start-line1 (Integer/parseInt (nth retval 1))
       :count1      (Integer/parseInt (nth retval 2))
       :start-line2 (Integer/parseInt (nth retval 3))
       :count2      (Integer/parseInt (nth retval 4))}
      ; else maybe no second length
      ; "@@ -1,5 +1 @@"
      (let [retval2 (re-find #"^@@ \-(\d+),(\d+) \+(\d+) .*$" s)]
        (if retval2 {:start-line1 (Integer/parseInt (nth retval2 1))
                     :count1      (Integer/parseInt (nth retval2 2))
                     :start-line2 (Integer/parseInt (nth retval2 3))
                     :count2      1}
          ; else no first length
          ;  "@@ -9 +9,5 @@ line 4"
          (let [retval3 (re-find #"^@@ \-(\d+) \+(\d+),(\d+) .*$" s)]
            (if retval3 {:start-line1 (Integer/parseInt (nth retval3 1))
                         :count1      0
                         :start-line2 (Integer/parseInt (nth retval3 2))
                         :count2      (Integer/parseInt (nth retval3 3))}
                  ; else
                  nil)))))))


(defn add-parsed-commit-changes
  " add :changes to map
    input: map
    output: map"
  [commit]
  (println "hash: " (:hash commit))
  (let [changes (->> commit
                     :lines
                     (map parse-diff-line)
                     (remove nil?))]
    (-> commit
        (assoc :changes changes))))
        ;(dissoc :lines))))

(defn- row->map
  " convert seq to map, with the given hashkey as key "
  [row hashkey]
  {hashkey
   row})

(defn- list-hashes-to-map
  [lm]
  (let [newmaps (map (fn [l] (row->map l (:hash l)))
                     lm)]
    (apply merge newmaps)))

(defn- add-count-change-sets [cs]
  (assoc cs :num-changes (count (:changes cs))))

(defn add-commit-ops
  [cs]
  ;(println "add-commit-ops: " cs)
  (assoc cs :change-ops (map (fn [x]
                               (pd/diff>ops x))
                          (:changes cs))))

(comment
  ((juxt :start-line1 :count1 :start-line2 :count2) {:start-line1 0, :count1 0, :start-line2 1, :count2 5})
  ,)


(defn add-change-commit-info
  [cs]
  (->> cs
       (map add-parsed-commit-changes)
       (map add-count-change-sets)
       (map add-commit-ops)))
       ;(filter (fn [x] (< (:num-changes x) 80)))))
       ;list-hashes-to-map))
    ;changesets))
    ;(->> (map merge cs changesets)
    ;     (map #(dissoc % :lines)))))

(defn gen-commit-hash-to-all-diffs
  " main entry point:
  input: none (global var with filename)
  output: list of maps "
  []
  (let [lines (clojure.string/split-lines (slurp INFILE))
        diffs (add-change-commit-info (split-by-commits lines {} nil))]
    diffs))


(comment
  (parse-diff-line "xxx")
  (parse-diff-line "@@ -7,7 +7,7 @@ by Gene Kim")

  (add-change-commit-info (split-by-commits lines {} nil))
  (add-change-commit-info (split-by-commits (take 2500 lines) {} nil))

  ; this is what gets called externally
  (gen-commit-hash-to-all-diffs))
