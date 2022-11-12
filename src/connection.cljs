(ns connection
  (:require ["pg$default" :as pg]
            ["pg-connection-string$default" :as pg-url-parser]
            ["ink$default" :refer [Box Text render]]
            ["ink-text-input$default" :refer [UncontrolledTextInput]]
            [reagent.core :as r]
            ["ink-big-text$default" :as Bigtext]
            [applied-science.js-interop :as j]))

(defonce pg-connection (r/atom nil))
(defonce conn-err (r/atom nil))


(defn connect-to-pg [url]
  (->
   (js/Promise. (fn [r] (r (pg/Client. (pg-url-parser/parse url)))))
   (.then (fn [client] (.connect client)))
   (.then (fn [client] (reset! pg-connection client)))
   (.then (fn [] (reset! conn-err nil)))
   (.catch (fn [err]
             (prn err)
             (reset! conn-err err)))))


(defn Connection []
  [:> Box {:flex-direction "column"}
   [:> Box
    [:> Bigtext {:text "PG UNUSED INDEXES"}]]
   [:> Box
    [:> Box
     [:> Text "Please connect to database via Postgres URL: "]]
    [:> Box
     [:> UncontrolledTextInput {:on-submit connect-to-pg}]]]
   [:> Box
    [:> Text {:color "red"} (j/get @conn-err :message)]]])


(render (r/as-element [Connection]))
