(ns graph-git-doc.repl-utils)

;
; namespace cleanup for repl
;

(defn ns-clean
  "Remove all internal mappings from a given name space or the current one if no parameter given."
  ([] (ns-clean *ns*))
  ([ns] (map #(ns-unmap ns %) (keys (ns-interns ns)))))