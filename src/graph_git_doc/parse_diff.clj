(ns graph-git-doc.parse-diff
  (:require
    [defun.core :refer [defun]]))

(defun compute-op
  ([0 0 c d] [:add c d])
  ([a b 0 0] [:delete a b])
  ([start-line1 count1 start-line2 count2]
   (cond
     ; same interval
     (and (= start-line1 start-line2)
          (= count1 count2))
     [:modify start-line1 count1]
     ; starts at the same location, but second is longer
     ;  {:start-line1 5, :count1 2, :start-line2 5, :count2 4}
     (and (= start-line1 start-line2)
          (> count2 count1)
          (not= count1 0))
     [[:modify start-line1 count1]
      [:add (+ start-line1 count1) (- count2 count1)]]
     ; count1 is zero
     ;  {:start-line1 9, :count1 0, :start-line2 9, :count2 5}
     (and (= start-line1 start-line2)
       (> count2 count1)
       (= count1 0))
     [:add start-line2 (dec count2)]
     ; starts at the same location, but second is longer
     ;  {:start-line1 1, :count1 5, :start-line2 1, :count2 0}
     (and (= start-line1 start-line2)
          (= 0 count2))
     [:delete start-line1 (dec count1)]
     ; delete
     ;  {:start-line1 12, :count1 6, :start-line2 12, :count2 2}
     (and (= start-line1 start-line2)
          (> count1 count2))
     [:delete start-line1 (- count1 count2)]

     :else nil)))



(defn diff>ops
  " input: {:start-line1 0, :count1 0, :start-line2 1, :count2 5}
    output: [[:add [1 2]]"
  [m]
  {:pre [(map? m)]}
  (let [{:keys [start-line1 count1 start-line2 count2]} m
        op (compute-op start-line1 count1 start-line2 count2)]
    op))


(comment
  (compute-op 0 0 1 1)

  (diff>ops nil)
  ,)
