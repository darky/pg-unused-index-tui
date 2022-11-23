(ns indexestest
  (:require ["uvu" :as uvu]
            ["uvu/assert" :refer [ok]]
            ["ink-testing-library$default" :as ink]
            ["rocket-pipes-slim" :as pipe]
            [clojure.string :as s]
            [indexes :refer [show-index-stats index-stats refresh-index-stats Indexes]]
            [reagent.core :as r]))


(uvu/test.before.each
 (fn []
   (.replaceUndo refresh-index-stats)
   (.replaceUndo show-index-stats)))


(uvu/test
 "show-index-stats should mutate @index-stats"
 (fn []
   (.replace show-index-stats #js [[0 #(identity #js {"rows" [{:foo "bar"}]})] [2 #()]])
   ((pipe/p
     #(reset! index-stats nil)
     #(show-index-stats)
     #(ok (not (nil? @index-stats)) "@index-stats not mutated")))))


(uvu/test
 "refresh-index-stats should call show-index-stats"
 (fn []
   (let [val (atom 0)]
     (.replace refresh-index-stats #js [[0 #()]])
     (.replace show-index-stats #js [[0 #(identity #js {"rows" [{:foo "bar"}]})] [2 #(swap! val inc)]])
     ((pipe/p
       #(refresh-index-stats)
       #(ok (= @val 1) "show-index-stats not called on refresh-index-stats"))))))


(uvu/test
 "should render empty indexes"
 (fn []
   (reset! index-stats [])
   ((pipe/p
     #(ink/render (r/as-element [:f> Indexes]))
     #(.lastFrame %)
     #(s/includes?
       % "No info about indexes")
     #(ok % "Can't render empty indexes")))))


(uvu/test
 "should render indexes table"
 (fn []
   (reset! index-stats [{:key "test check"}])
   ((pipe/p
     #(ink/render (r/as-element [:f> Indexes]))
     #(.lastFrame %)
     #(s/includes?
       % "test check")
     #(ok % "Can't render indexes table")))))
