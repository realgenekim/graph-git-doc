(ns graph-git-doc.git-log-text
  "
  input: git-log.txt (generated by Makefile, which runs
    'git log'),

  output: parsed diffs
          {commithash {:start-line1 4, :count1 3, :start-line2 4, :count2 3}")

(def INFILE "git-log.txt")

(def lines (clojure.string/split-lines (slurp INFILE)))

(def short (take 300 lines))

(defn- commit-line? [s]
  (re-find #"^commit (\w+)$" s))

(defn- split-by-commits
  [lines current-set hash-list]
  (let [l (first lines)]
    (when false
      (do
        (println "line: " l)
        (println "set:  " current-set)
        (println "list: " hash-list)
        (println "---")))
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




#_ (split-by-commits ["commit 123"
                      "abc"
                      "def"
                      "commit abc"
                      "hij"
                      "klm"] {} nil)

#_ (def x (split-by-commits (take 500 lines) {} nil))

(defn- parse-diff-line [s]
  " @@ -startline1,count1 +startline2,count2 @@
        https://stackoverflow.com/questions/8259851/using-git-diff-how-can-i-get-added-and-modified-lines-numbers
    returns nil when not valid "
  (let [retval (re-find #"^@@ \-(\d+),(\d+) \+(\d+),(\d+).*$" s)]
    (if retval
      {:start-line1 (Integer/parseInt (nth retval 1))
       :count1      (Integer/parseInt (nth retval 2))
       :start-line2 (Integer/parseInt (nth retval 3))
       :count2      (Integer/parseInt (nth retval 4))}
      nil)))

(defn process-commit
  [commit]
  (println "hash: " (:hash commit))
  (let [changes (->> commit
                     :lines
                     (map parse-diff-line)
                     (remove nil?))]
    (-> commit
        (assoc :changes changes)
        (dissoc :lines))))

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

(defn- count-change-sets [cs]
  (assoc cs :num-changes (count (:changes cs))))


(defn- find-diffs [cs]
  (->> cs
       (map process-commit)
       (map count-change-sets)
       (filter (fn [x] (< (:num-changes x) 80)))
       list-hashes-to-map))
    ;changesets))
    ;(->> (map merge cs changesets)
    ;     (map #(dissoc % :lines)))))

(defn gen-commit-hash-to-all-diffs []
  (let [lines (clojure.string/split-lines (slurp INFILE))
        diffs (find-diffs (split-by-commits lines {} nil))]
    diffs))


(comment
  (parse-diff-line "xxx")
  (parse-diff-line "@@ -7,7 +7,7 @@ by Gene Kim")

  (find-diffs (split-by-commits lines {} nil))
  (find-diffs (split-by-commits (take 2500 lines) {} nil))

  ; this is what gets called externally
  (gen-commit-hash-to-all-diffs))
