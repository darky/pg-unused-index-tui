(ns connection
  (:require ["pg$default" :as pg]
            ["pg-connection-string$default" :as pg-url-parser]
            ["ink$default" :refer [Box Text]]
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
   (.then (fn [client]
            (-> (js/Promise. #(%))
                (.then (.connect client))
                (.then #(reset! pg-connection client))
                (.then #(fetch-indexes))
                (.then #(reset! conn-err nil)))))
   (.catch #(reset! conn-err %))))

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
