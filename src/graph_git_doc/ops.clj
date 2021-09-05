(ns graph-git-doc.ops
  (:require
    [graph-git-doc.git-log-text :as glog]
    [portal.api :as p]))

(comment
  (p/open) ; Open a new inspector
  (add-tap #'p/submit) ; Add portal as a tap> target
  (tap> :hello)
  ,)

;
; get the log of commits
;

;(def commits (glog/gen-commit-hash-to-all-diffs))
(def commits (->> (glog/list-of-commits)
                  (glog/add-change-commit-info)))

(comment
  commits
  (tap> commits)
  ,)
