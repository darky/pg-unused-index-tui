(ns index
  (:require
   [connection :refer [render-connection]]))

(render-connection (.-5 js/process.argv))
