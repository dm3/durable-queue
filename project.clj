(defproject factual/durable-queue "0.1.6-SNAPSHOT"
  :description "An in-process task-queue that is backed by disk."
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[com.taoensso/nippy "2.13.0"]
                 [primitive-math "0.1.6"]
                 [byte-streams "0.2.3"]]
  :profiles {:dev {:dependencies [[org.clojure/clojure "1.8.0"]
                                  [criterium "0.4.3"]
                                  [codox-md "0.2.0" :exclusions [org.clojure/clojure]]]}}
  :global-vars {*warn-on-reflection* true}
  :test-selectors {:default #(not (some #{:benchmark :stress} (keys %)))
                   :benchmark :benchmark
                   :stress :stress}
  :plugins [[codox "0.10.3"]]
  :codox {:writer codox-md.writer/write-docs
          :namespaces [durable-queue]}
  :jvm-opts ^:replace ["-server" "-Xmx100m"])
