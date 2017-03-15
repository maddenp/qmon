(defproject qmon "1.1"
  :dependencies [[org.clojure/tools.cli "0.3.5"]
                 [org.clojure/clojure "1.6.0"]
                 [com.github.kyleburton/clj-xpath "1.4.11"]]
  :description "A simple PBS job-monitor GUI"
  :license {:name "Apache License Version 2.0" :url "http://www.apache.org/licenses/LICENSE-2.0.html"}
  :main ^:skip-aot qmon.core
  :profiles {:uberjar {:aot :all}}
  :target-path "target/%s"
  :uberjar-name "qmon.jar"
  :url "https://github.com/maddenp/qmon")
