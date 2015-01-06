(defproject songcontest "0.1.0"
  :dependencies [[org.clojure/clojure "1.7.0-alpha4"]
                 [org.clojure/clojurescript "0.0-2665"]
                 [reagent "0.4.3"]
                 [reagent-forms "0.2.7"]
                 [reagent-utils "0.1.1"]
                 [secretary "1.2.1"]
                 [cljs-ajax "0.3.4"]
                 [ring "1.3.2"]
                 [ring-edn "0.1.0"]
                 [compojure "1.3.1"]]
  :plugins [[lein-cljsbuild "1.0.4"]
            [lein-ring "0.8.13"]]
  :hooks [leiningen.cljsbuild]
  :profiles {:prod {:cljsbuild
                    {:builds
                     {:client {:compiler
                               {:optimizations :advanced
                                :preamble ^:replace ["reagent/react.min.js"]
                                :pretty-print false}}}}}
             :srcmap {:cljsbuild
                      {:builds
                       {:client {:compiler
                                 {:source-map "target/client.js.map"
                                  :source-map-path "client"}}}}}}
  :source-paths ["src"]
  :cljsbuild
  {:builds
   {:client {:source-paths ["src"]
             :compiler
             {:preamble ["reagent/react.js"]
              :output-dir "target/client"
              :output-to "target/client.js"
              :pretty-print true}}}}
  :ring {:handler songcontest/server-routes})
