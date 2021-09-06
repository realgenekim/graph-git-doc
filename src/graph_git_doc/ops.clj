(comment
  (glog/run-git-command! "../test-git-repo" "manuscript.md" "git-log.txt")
  (def commits (->> (glog/list-of-commits "git-log.txt")
                    (glog/add-change-commit-info))))
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

(def commits (create-commits!))
  (tap> commits)
