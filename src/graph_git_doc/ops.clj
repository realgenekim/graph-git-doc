; stage 1
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


;
; stage 2
;   add the word count and number of lines
;

(comment
  (->> commits
       (map :hash))

  ,)

