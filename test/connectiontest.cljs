(ns connectiontest
  (:require ["uvu" :as uvu]
            ["uvu/assert" :refer [ok is]]
            ["ink-testing-library$default" :as ink]
            ["rocket-pipes-slim" :as pipe]
            [clojure.string :as s]
            [global :refer [pg-connection]]
            [connection :refer [connect-to-pg Connection conn-err on-submit]]
            [indexes :refer [show-index-stats]]
            [reagent.core :as r]
            ["timers/promises" :refer [setTimeout]]))


(uvu/test.before.each
 (fn []
   (.replaceUndo connect-to-pg)
   (.replaceUndo show-index-stats)))


(uvu/test
 "basic render connection"
 (fn []
   ((pipe/p
     #(ink/render (r/as-element [Connection]))
     #(.lastFrame %)
     #(s/includes?
       % "Please connect to database via Postgres URL")
     #(ok % "incorrect render")))))

(uvu/test
 "render connection with url"
 (fn []
   ((pipe/p
     #(ink/render (r/as-element [Connection "test"]))
     #(.lastFrame %)
     #(s/includes?
       % "Please connect to database via Postgres URL: test")
     #(ok % "incorrect render")))))


(uvu/test
 "render connection with error"
 (fn []
   (reset! conn-err #js {:message "test error"})
   ((pipe/p
     #(ink/render (r/as-element [Connection]))
     #(.lastFrame %)
     #(s/includes?
       % "test error")
     #(ok % "incorrect render")))))


(uvu/test
 "set connection atom on db connect"
 (fn []
   (.replace connect-to-pg #js [[2 #()] [3 #()]])
   ((pipe/p
     #(connect-to-pg "")
     #(ok (not (nil? @pg-connection)) "pg-connection atom not mutated")))))


(uvu/test
 "show-index-stats should be called on db connection"
 (fn []
   (let [val (atom 0)]
     (.replace show-index-stats #js [[0 #(swap! val inc)] [1 #()] [2 #()]])
     (.replace connect-to-pg #js [[2 #()] [3 #(show-index-stats)]])
     ((pipe/p
       #(connect-to-pg "")
       #(is @val 1 "show-index-stats not called"))))))


(uvu/test
 "on success db connection error reset"
 (fn []
   (.replace connect-to-pg #js [[2 #()] [3 #()]])
   ((pipe/p
     #(reset! conn-err {:message "err"})
     #(connect-to-pg "")
     #(ok (nil? @conn-err) "conn-err atom not reset")))))


(uvu/test
 "set conn-err atom on DB connection error"
 (fn []
   (.replace connect-to-pg #js [[2 #(throw {:message "test err"})] [3 #()]])
   ((pipe/p
     #(reset! conn-err nil)
     #(on-submit "test")
     #(ok (not (nil? @conn-err)) "conn-err atom not mutated")))))


(uvu/test
 "render passed connection url"
 (fn []
   ((pipe/p
     #(ink/render (r/as-element [Connection]))
     (fn [rend]
       ((pipe/p
         #(.-stdin rend)
         #(.write % "test url")
         #(.lastFrame rend))))
     #(s/includes?
       % "test url")
     #(ok % "can't render passed url")))))


(uvu/test
 "call on-submit with url when press Enter"
 (fn []
   (let [url (atom nil)]
     (.replace connect-to-pg #js [[0 #(reset! url %)] [2 #()] [3 #()]])
     ((pipe/p
       #(ink/render (r/as-element [:f> Connection]))
       #(.-stdin %)
       (fn [stdin]
         ((pipe/p
           #(setTimeout 1)
           #(.write stdin "test url")
           #(setTimeout 1)
           #(.write stdin "\r"))))
       #(ok (= @url "test url") "can't submit url"))))))
