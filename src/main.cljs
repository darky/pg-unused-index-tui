#!/usr/bin/env -S nbb --classpath "src"

(ns main
  (:require
   [connection :refer [render-connection]]))

(render-connection (.-5 js/process.argv))
