(ns graph-git-doc.ops
  (:require
    [graph-git-doc.git-log-text :as glog]
    [graph-git-doc.git-wrappers :as gw]
    [portal.api :as p]))

(comment
  (p/open) ; Open a new inspector
  (add-tap #'p/submit) ; Add portal as a tap> target
  (tap> :hello)
  ,)

;
; stage 1
; get the log of commits
;
; we have each commit, as well the diffs
;
;{:num-changes 1,
; :hash "a0ef4824c7a4478103c705d7c30a557f70c2d78d",
; :lines ["commit a0ef4824c7a4478103c705d7c30a557f70c2d78d"
;         "Author: Gene Kim <>"
;         "Date:   Fri Sep 3 19:48:11 2021 -0700"
;         ""
;         "    initial"
;         "---"
;         " manuscript.md | 5 +++++"
;         " 1 file changed, 5 insertions(+)"
;         ""
;         "diff --git a/manuscript.md b/manuscript.md"
;         "new file mode 100644"
;         "index 0000000..94c99a3"
;         "--- /dev/null"
;         "+++ b/manuscript.md"
;         "@@ -0,0 +1,5 @@"
;         "+line 1"
;         "+line 2"
;         "+line 3"
;         "+line 4"
;         "+line 5"],
; :change-ops ([:add 1 5]),
; :changes ({:start-line1 0, :count1 0, :count2 5, :start-line2 1})}


;(def commits (glog/gen-commit-hash-to-all-diffs))
(comment
  (glog/run-git-command! "../test-git-repo" "manuscript.md" "git-log.txt")
  (def commits (->> (glog/list-of-commits "git-log.txt")
                    (glog/add-change-commit-info))))


(comment
  commits
  (tap> commits)
  ,)

;
; stage 2
;   add the word count and number of lines
;

(comment
  (def commits (->> commits
                    (map #(gw/add-word-stats-to-git-commit! % "../test-git-repo" "manuscript.md"))
                    (map #(gw/add-timestamp-to-git-commit! % "../test-git-repo")))))

(defn create-commits!
  []
  (let [
        repo-dir "../test-git-repo"
        repo-dir "/Users/genekim/book5"
        manuscript "manuscript.md"
        manuscript "manuscript-unicorn/manuscript.mmd"
        outfile "git-log.txt"

        out (glog/run-git-command! repo-dir manuscript outfile)

        commit1 (->> (glog/list-of-commits outfile)
                     (glog/add-change-commit-info))

        commit2 (->> commit1
                  (map #(gw/add-word-stats-to-git-commit! % repo-dir manuscript))
                  (map #(gw/add-timestamp-to-git-commit! % repo-dir)))]

    commit2))

(defn create-commits-sample!
  []
  (let [
        repo-dir "/Users/genekim/book5"
        repo-dir "../test-git-repo"
        manuscript "manuscript-unicorn/manuscript.mmd"
        manuscript "manuscript.md"
        outfile "git-log.txt"

        out (glog/run-git-command! repo-dir manuscript outfile)

        commit1 (->> (glog/list-of-commits outfile)
                     (glog/add-change-commit-info))

        commit2 (->> commit1
                  (map #(gw/add-word-stats-to-git-commit! % repo-dir manuscript))
                  (map #(gw/add-timestamp-to-git-commit! % repo-dir)))]

    commit2))

(def commits (create-commits!))
(def commits-sample (create-commits-sample!))

(comment
  (->> commits
       (map :hash))
  (tap> (->> commits
          (map :changes)))
  (tap> (->> commits
          (map :change-ops)))

  (tap> commits)
  (count commits)

  (->> commits
       (map gw/add-word-stats-to-git-commit!)
       (map gw/add-timestamp-to-git-commit!))

  ,)


