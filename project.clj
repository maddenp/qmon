(defproject qmon "1.0"
  :dependencies [[org.clojure/clojure "1.6.0"]]
  :description "A simple PBS job-monitor GUI"
  :license {:name "Apache License Version 2.0" :url "http://www.apache.org/licenses/LICENSE-2.0.html"}
  :main ^:skip-aot qmon.core
  :profiles {:uberjar {:aot :all}}
  :target-path "target/%s"
  :uberjar-name "qmon.jar"
  :url "https://github.com/maddenp/qmon")
