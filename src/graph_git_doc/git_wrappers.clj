(ns graph-git-doc.git-wrappers
  (:require
    [archaeologist.core :as a]
    [archaeologist.git :as agit]
    [archaeologist.fs :as fs]
    [clj-jgit.porcelain :as git]
    [clojure.string :as s]))

(defn foo
  "I don't do a whole lot."
  [x]
  (println x "Hello, World!"))

;(def path "/Users/genekim/book5")
(def path "../test-git-repo")

(comment
  ;(def g (git/discover-repo path))

  (def g (git/load-repo path))
  (git/git-status g)
  (git/git-branch-list g)
  (def log (git/git-log g)))



(comment

  (:name (bean (first log))))


  ;{:encoding       #object[sun.nio.cs.UTF_8 0x5a72d4e4 "UTF-8"],
  ; :parentCount    0,
  ; :footerLines    [],
  ; :tree           #object[org.eclipse.jgit.revwalk.RevTree 0x7271bb9e "tree 9e9d436f3cf22595422fb369f379879da2da4fe9 ------"],
  ; :committerIdent #object[org.eclipse.jgit.lib.PersonIdent
  ;                         0x510ffd
  ;                         "PersonIdent[Gene Kim, genek@realgenekim.me, Tue Feb 28 09:23:57 2017 -0800]"],
  ; :name           "0a1460bf31563c60ca6789a307a0a404975df884",
  ; :type           1,
  ; :firstByte      10,
  ; :authorIdent    #object[org.eclipse.jgit.lib.PersonIdent
  ;                         0x70cbd142
  ;                         "PersonIdent[Gene Kim, genek@realgenekim.me, Tue Feb 28 09:23:57 2017 -0800]"],
  ; :encodingName   nil,
  ; :id             #object[org.eclipse.jgit.revwalk.RevCommit
  ;                         0x48c2add4
  ;                         "commit 0a1460bf31563c60ca6789a307a0a404975df884 1488302637 ----sp"],
  ; :class          org.eclipse.jgit.revwalk.RevCommit,
  ; :commitTime     1488302637,
  ; :parents        #object["[Lorg.eclipse.jgit.revwalk.RevCommit;" 0x37905219 "[Lorg.eclipse.jgit.revwalk.RevCommit;@37905219"],
  ; :shortMessage   "initial checkin",
  ; :fullMessage    "initial checkin\n",
  ; :rawBuffer      #object["[B" 0x5138a27b "[B@5138a27b"]})

(defn xform-log
  " turn jgit data structure to more clj-friendly data "
  [l]
  {:date (.getWhen (:authorIdent l))
   :hash (:name l)})

(defn get-git-log!
  " get list of commits in map form "
  []
  (let [repo (git/load-repo path)
        logs (git/git-log repo)]
    (->> logs
         (map bean)
         (map #(select-keys % [:name :authorIdent]))
         (map xform-log))))

(comment
  (get-git-log!)
  ,)

(defn get-manuscript-by-commit-hash!
  " for given commit, return string of entire file
    if file doesn't exist in commit, return nil "
  [commit]
  (let [fullpath (str path "/.git/")
        mmd "manuscript.md"]
    (a/with-repository [repo (agit/open-repository fullpath)]
      (try
        (->> (a/read-file repo commit mmd)
             slurp)
        (catch Exception e
          nil)))))


(defn count-lines
  [text]
  (some->> text
       clojure.string/split-lines
       count))

(defn count-words
  [text]
  (some-> text
          (s/split #"\s+")
          count))

(comment
  (def text "ab   d e 123  dd\n 123")
  (s/split text #"\s+"))

(defn add-word-stats-to-git-commit!
  [commit]
  (let [hash (:hash commit)
        text (get-manuscript-by-commit-hash! hash)
        ;logs-with-lines (merge-in-manuscript-line-counts logs)]
        stats {:stats-num-lines (count-lines text)
               :stats-num-words (count-words text)}]
    (merge commit stats)))

(comment
  (def commits (get-git-log!))
  (add-word-stats-to-git-commit! (second commits))
  (->> commits
    (map add-word-stats-to-git-commit!))

  ,)

(defn add-timestamp-to-git-commit!
  "input: map
   output: map with merged new info"
  [cs]
  (let [hash (:hash cs)
        commits (get-git-log!)
        commit  (->> commits
                     (filter #(= (:hash %)
                                 hash))
                     first)]
    (merge commit cs)))


(comment
  (add-timestamp-to-git-commit! {:hash "a0ef4824c7a4478103c705d7c30a557f70c2d78d"
                                 :abc 123})
  (add-timestamp-to-git-commit! nil)
  ,)







#_(get-git-log!)
#_(count (get-git-log!))


;(git/get-blob g "6ec864b68279d8bbb6c8c228239346cb2234ad1a" "./manuscript-unicorn/manuscript.mmd")

;(with-repository [repo (git/open-repository "test/fixtures/git")]
;                 (list-files repo (get-default-version repo)))

(comment
  (def repo (agit/open-repository (str path "/.git/")))

  (def mmd "manuscript.md")
  (def mmd "manuscript.md")

  (a/with-repository [repo (agit/open-repository (str path "/.git/"))]
    (let [mmd "manuscript.md"]
      (count (slurp (a/read-file repo "HEAD" mmd)))))


  (a/list-files repo (a/get-default-version repo))

  ;(def mmd "manuscript-unicorn/manuscript.mmd")

  (def mmd-text (slurp (a/read-file repo "HEAD" mmd)))
  (def mmd-text (slurp (a/read-file repo "4f09c82e174d919e7dd42c913c6772d7a144af32" mmd)))

  (def lines (clojure.string/split-lines mmd-text))

  (first lines)
  (count lines)

  (def mmd-text (slurp (a/read-file repo
                                    "4f09c82e174d919e7dd42c913c6772d7a144af32"
                                    mmd))))