(ns connection
  (:require ["pg$default" :as pg]
            ["pg-connection-string$default" :as pg-url-parser]
            ["ink$default" :refer [Box Text render]]
            ["ink-text-input$default" :refer [UncontrolledTextInput]]
            [reagent.core :as r]
            ["ink-big-text$default" :as Bigtext]
            [applied-science.js-interop :as j]
            [indexes :refer [show-index-stats]]
            [global :refer [pg-connection]]
            ["rocket-pipes-slim" :as pipe]))


(defonce ^:private conn-err (r/atom nil))


(def ^:private connect-to-pg
  (pipe/p
   #(pg/Client. (pg-url-parser/parse %))
   #(reset! pg-connection %)
   #(.connect %)
   #(show-index-stats)
   #(reset! conn-err nil)))


(defn- Connection [url]
  [:> Box {:flex-direction "column"}
   [:> Box
    [:> Bigtext {:text "PG UNUSED INDEX"}]]
   [:> Box
    [:> Box
     [:> Text "Please connect to database via Postgres URL: "]]
    [:> Box
     [:> UncontrolledTextInput
      {:on-submit
       (fn [url]
         (->
          (connect-to-pg url)
          (.catch #(reset! conn-err %))))
       :initial-value url}]]]
   [:> Box
    [:> Text {:color "red"} (j/get @conn-err :message)]]])


(defn render-connection [url] (render (r/as-element [Connection url])))
