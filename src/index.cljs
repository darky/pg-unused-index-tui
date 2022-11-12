(ns index
  (:require
   ["ink$default" :refer [render]]
   [reagent.core :as r]
   [connection :refer [Connection]]))


(render (r/as-element [Connection]))
