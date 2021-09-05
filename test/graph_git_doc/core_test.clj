(ns graph-git-doc.core-test
  (:require [clojure.test :refer :all]
            [graph-git-doc.git-log-text :as glt]
            [graph-git-doc.parse-diff :as pd]))

(deftest a-test
  (testing "FIXME, I fail."
    (is (= 1 1))))

(deftest git-log
  (testing "add"
    (is (= {:start-line1 0, :count1 0, :start-line2 1, :count2 5}
           (glt/parse-diff-line "@@ -0,0 +1,5 @@"))))
  (testing "delete 2"
    (is (= {:start-line1 1, :count1 2, :start-line2 0, :count2 0}
           (glt/parse-diff-line "@@ -1,2 +0,0 @@"))))
  (testing "modify 2"
    (is (= {:start-line1 3, :count1 4, :start-line2 3, :count2 4}
           (glt/parse-diff-line "@@ -3,4 +3,4 @@"))))
  (testing "modify 3 add 2"
    (is (= {:start-line1 5, :count1 2, :start-line2 5, :count2 4}
           (glt/parse-diff-line "@@ -5,2 +5,4 @@ line 3333"))))
  (testing ""
    (is (= {:start-line1 1, :count1 5, :start-line2 1, :count2 1}
          (glt/parse-diff-line "@@ -1,5 +1 @@"))))

  ,)

(deftest transform-to-add-modify-deletes
  (testing ""
    (is (= [:add 1 5]
           (apply pd/compute-op (vals {:start-line1 0, :count1 0, :start-line2 1, :count2 5}))))
    (is (= [:delete 1 2]
           (apply pd/compute-op (vals {:start-line1 1, :count1 2, :start-line2 0, :count2 0}))))
    (is (= [:modify 3 4]
          (apply pd/compute-op (vals {:start-line1 3, :count1 4, :start-line2 3, :count2 4}))))
    (is (= [[:modify 5 2] [:add 7 2]]
          (apply pd/compute-op (vals {:start-line1 5, :count1 2, :start-line2 5, :count2 4})))))
  (is (= [:delete 1 4]
        (apply pd/compute-op (vals {:start-line1 1, :count1 5, :start-line2 1, :count2 0}))))

  ,)



