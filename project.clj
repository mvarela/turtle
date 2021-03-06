(defproject rm-hull/turtle "0.1.10"
  :description "A lightweight Clojure/ClojureScript turtle-graphics library for SVG, Graphics2D and HTML5 canvas rendering"
  :url "https://github.com/rm-hull/turtle"
  :license {
    :name "The MIT License (MIT)"
    :url "http://opensource.org/licenses/MIT"}
  :dependencies [
    [org.clojure/clojure "1.8.0"]
    [org.clojure/clojurescript "1.9.293"]
    [hipo "0.5.2"]
    [hiccup "1.0.5"]
    [rm-hull/monet "0.3.0"]]
  :scm {
    :url "git@github.com:rm-hull/turtle.git"}
  :hooks [leiningen.dalap]
  :source-paths ["src" "target/generated-src"]
  :cljsbuild {
    :builds [{
      :source-paths ["target/generated-src"]
      :id "main"
      :jar true}]}
  :min-lein-version "2.6.1"
  :profiles {
    :dev {
      :global-vars {*warn-on-reflection* true}
      :plugins [
        [lein-cljfmt "0.5.6"]
        [lein-cloverage "1.0.9"]
        [lein-cljsbuild "1.1.4"]
        [com.birdseye-sw/lein-dalap "0.1.1"]]}})
