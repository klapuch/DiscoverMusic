(defproject discover-music "0.1.0-SNAPSHOT"
  :description "Discover new music"
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [clj-http "3.10.0"]
                 [org.jsoup/jsoup "1.12.1"]
                 [cheshire "5.9.0"]
                 [digest "1.4.9"]]
  :main ^:skip-aot ktb.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
