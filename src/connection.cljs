(ns connection
  (:require ["pg$default" :as pg]
            ["pg-connection-string$default" :as pg-url-parser]
            ["ink$default" :refer [Box Text render]]
            ["ink-text-input$default" :refer [UncontrolledTextInput]]
            [reagent.core :as r]
            ["ink-big-text$default" :as Bigtext]
            [applied-science.js-interop :as j]
            [indexes :refer [fetch-indexes]]
            [global :refer [pg-connection]]))


(defonce ^:private conn-err (r/atom nil))


(defn- connect-to-pg [url]
  (->
   (js/Promise. #(% (pg/Client. (pg-url-parser/parse url))))
   (.then #(reset! pg-connection %))
   (.then #(.connect %))
   (.then #(fetch-indexes))
   (.then #(reset! conn-err nil))
   (.catch #(reset! conn-err %))))


(defn Connection [url]
  [:> Box {:flex-direction "column"}
   [:> Box
    [:> Bigtext {:text "PG UNUSED INDEX"}]]
   [:> Box
    [:> Box
     [:> Text "Please connect to database via Postgres URL: "]]
    [:> Box
     [:> UncontrolledTextInput {:on-submit connect-to-pg :initial-value url}]]]
   [:> Box
    [:> Text {:color "red"} (j/get @conn-err :message)]]])


(defn render-connection [url] (render (r/as-element [Connection url])))
