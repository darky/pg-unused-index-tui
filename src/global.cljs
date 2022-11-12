(ns global
  (:require [reagent.core :as r]))

(defonce pg-connection (r/atom nil))
