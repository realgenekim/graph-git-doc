(ns graph-git-doc.core
  (:require [clj-jgit.porcelain :as git]
            [archaeologist.core :as a]
            [archaeologist.git :as agit]
            [archaeologist.fs :as fs]))

(def path "/Users/genekim/book5")

(defn xform-log
  " turn jgit data structure to more clj-friendly data "
  [l]
  {:date   (.getWhen (:authorIdent l))
   :commit (:name l)})

(defn get-log
  " get list of commits in map form "
  []
  (let [repo (git/load-repo path)
        logs (git/git-log repo)]
    (->> logs
         (map bean)
         (map #(select-keys % [:name :authorIdent]))
         (map xform-log))))


(defn get-manuscript-by-commit
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

(defn merge-in-manuscript-line-counts
  [logs]
  (let [line-counts (->> logs
                         (map :commit)
                         (map #(get-manuscript-by-commit %))
                         (map #(assoc {} :lines %)))]
    (map merge logs line-counts)))

(defn get-commit-with-lines []
  (let [logs (get-log)
        logs-with-lines (merge-in-manuscript-line-counts logs)]
    logs-with-lines))



#_ (count (get-commit-with-lines))
#_ (def commits (get-commit-with-lines))
#_ (map :lines commits)

