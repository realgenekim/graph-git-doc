(defproject graph-git-doc "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1-beta1"]
                 ;[metasoarous/oz "1.5.7-SNAPSHOT"]
                 [metasoarous/oz "1.5.6"]
                 [org.clojure/data.csv "0.1.4"]
                 [clj-jgit "0.8.10"]
                 [clojure.java-time "0.3.2"]
                 [archaeologist "0.1.1"]
                 [defun "0.3.1"]
                 [djblue/portal "0.14.0"]]


                 ;[planisphere "0.1.9"]]
  :test-paths ["test"]
  :repl-options {:init-ns graph-git-doc.manuscript-lines})
