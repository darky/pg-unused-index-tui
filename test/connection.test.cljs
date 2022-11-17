(ns connection.test
  (:require ["uvu" :as uvu]
            ["uvu/assert" :refer [ok is]]
            ["ink-testing-library$default" :as ink]
            ["rocket-pipes-slim" :as pipe]
            [clojure.string :as s]
            [global :refer [pg-connection]]
            [connection :refer [connect-to-pg Connection conn-err on-submit]]
            [indexes :refer [show-index-stats]]
            [reagent.core :as r]))


(uvu/test
 "basic render connection"
 (fn [] (let [rend (ink/render
                    (r/as-element [Connection]))]
          ((pipe/p
            #(js->clj rend)
            #((get % "lastFrame"))
            #(s/includes?
              % "Please connect to database via Postgres URL")
            #(ok % "incorrect render"))))))

(uvu/test
 "render connection with url"
 (fn [] (let [rend (ink/render (r/as-element [Connection "test"]))
              rend-clj (js->clj rend)]
          ((pipe/p
            #((get rend-clj "lastFrame"))
            #(s/includes?
              % "Please connect to database via Postgres URL: test")
            #(ok % "incorrect render"))))))


(uvu/test
 "render connection with error"
 (fn []
   (reset! conn-err #js {:message "test error"})
   (let [rend (ink/render (r/as-element [Connection]))
         rend-clj (js->clj rend)]
     ((pipe/p
       #((get rend-clj "lastFrame"))
       #(s/includes?
         % "test error")
       #(ok % "incorrect render"))))))


(uvu/test
 "set connection atom on db connect"
 (fn [] (with-redefs
         [connect-to-pg (.replace connect-to-pg #js [[2 #()] [3 #()]])]
          ((pipe/p
            #(connect-to-pg "")
            #(ok (not (nil? @pg-connection)) "pg-connection atom not mutated"))))))


(uvu/test
 "show-index-stats should be called on db connection"
 (fn [] (let [val (atom 0)]
          (with-redefs
           [show-index-stats (.replace show-index-stats #js [[0 #(swap! val inc)] [1 #()] [2 #()]])
            connect-to-pg (.replace connect-to-pg #js [[2 #()] [3 #(show-index-stats)]])]
            ((pipe/p
              #(connect-to-pg "")
              #(is @val 1 "show-index-stats not called")))))))


(uvu/test
 "on success db connection error reset"
 (fn [] (with-redefs
         [connect-to-pg (.replace connect-to-pg #js [[2 #()] [3 #()]])]
          ((pipe/p
            #(reset! conn-err {:message "err"})
            #(connect-to-pg "")
            #(ok (nil? @conn-err) "conn-err atom not reset"))))))


(uvu/test
 "set conn-err atom on DB connection error"
 (fn []
   (with-redefs
    [connect-to-pg (.replace connect-to-pg #js [[2 #(throw {:message "test err"})] [3 #()]])]
     ((pipe/p
       #(reset! conn-err nil)
       #(on-submit "test")
       #(ok (not (nil? @conn-err)) "conn-err atom not mutated"))))))


(uvu/test.run)
