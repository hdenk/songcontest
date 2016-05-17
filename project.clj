(defproject songcontest "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"
    :dependencies [[org.clojure/clojure "1.8.0"]
                   [org.clojure/clojurescript "1.8.51"]
                   [org.clojure/core.async "0.2.374"]                 
                   [ring-server "0.4.0"]
                   [fogus/ring-edn "0.2.0"]
                   [compojure "1.3.4"]
                   [hiccup "1.0.5"]
                   [liberator "0.14.1"]
                   [prismatic/schema "1.1.1"]
                   [clj-json "0.5.3"]
                   [org.webjars/bootstrap "3.2.0"]
                   [org.clojure/java.jdbc "0.6.0-rc2"]
                   [com.h2database/h2 "1.4.189"]
                   [reagent "0.5.0"]
                   [reagent-forms "0.5.23"]
                   [cljs-http "0.1.30"]]

  :plugins [[lein-cljsbuild "1.1.3"]]

  :clean-targets ^{:protect false} [:target-path :compile-path "resources/public/out"]

  :source-paths ["src"]

  :profiles {:dev 
             {:plugins [[lein-figwheel "0.5.2"]]
              :dependencies [[javax.servlet/servlet-api "2.5"]
                             [ring/ring-mock "0.3.0"]]
              :figwheel {:http-server-root "public"
                         :server-port 3449}}}

  :cljsbuild {:builds [{:id "reagent"
                        :source-paths ["src-cljs"]
                        :figwheel true
                        :compiler {:output-to "resources/public/app.js"
                                   :output-dir "resources/public/out"
                                   :optimizations :none
                                   :asset-path "/out"
                                   :main "songcontest.app"
                                   :source-map true}}]}

  :repl-options {:init-ns songcontest.repl}
  
  :global-vars {*print-length* 20})
