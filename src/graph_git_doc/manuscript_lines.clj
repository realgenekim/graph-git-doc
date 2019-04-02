(ns graph-git-doc.manuscript-lines
  " this extracts for each commit the number of lines in the
    manuscript.mmd files "
  (:require [clj-jgit.porcelain :as git]
            [archaeologist.core :as a]
            [archaeologist.git :as agit]
            [archaeologist.fs :as fs]))

(def path "/Users/genekim/book5")

(defn- xform-log
  " turn jgit data structure to more clj-friendly data
    extract just the date, commit hash"
  [l]
  {:date   (.getWhen (:authorIdent l))
   :commit (:name l)})

(defn- get-log!
  " load in all the commits in map form "
  []
  (let [repo (git/load-repo path)
        logs (git/git-log repo)]
    (->> logs
         (map bean)
         (map #(select-keys % [:name :authorIdent]))
         (map xform-log))))


(defn- get-manuscript-by-commit!
  " for given commit, return string of entire file
    if file doesn't exist in commit, return nil "
  [commit]
  (let [fullpath (str path "/.git/")
        mmd "manuscript-unicorn/manuscript.mmd"]
    (a/with-repository [repo (agit/open-repository fullpath)]
                       (try
                         (->> (a/read-file repo commit mmd)
                              slurp
                              clojure.string/split-lines
                              count)
                         (catch Exception e
                           nil)))))

(defn- merge-in-manuscript-line-counts
  [logs]
  (let [line-counts (->> logs
                         (map :commit)
                         (map #(get-manuscript-by-commit! %))
                         (map #(assoc {} :lines %)))]
    (map merge logs line-counts)))

(defn get-commit-with-lines []
  (let [logs            (get-log!)
        logs-with-linecount (merge-in-manuscript-line-counts logs)]
    logs-with-linecount))



#_ (count (get-commit-with-lines))
#_ (def commits (get-commit-with-lines))
#_ (map :lines commits)

(comment
  (graph-git-doc.utils/ns-clean)

  ;
  (count (get-commit-with-lines))
  ; => 125

  ; get all the commmits
  (def commits (get-commit-with-lines))

  (map :lines commits)
  (map #(select-keys % [:lines :date]) commits))


(defn- main [] (println "hello"))
