(ns indexes
  (:require
   ["ink$default" :refer [render Text Box useInput Newline]]
   ["ink-table$default" :as Table]
   [reagent.core :as r]
   [global :refer [pg-connection]]
   ["rocket-pipes-slim" :as pipe]
   [clojure.string :as s]))


(defonce ^:private index-stats (r/atom []))


(declare Indexes)


(def show-index-stats
  (pipe/p
   #(.query @pg-connection "
SELECT
  idstat.relname AS TABLE_NAME,
  indexrelname AS index_name,
  idstat.idx_scan AS index_scans_count,
  pg_size_pretty(pg_relation_size(indexrelid)) AS index_size,
  tabstat.idx_scan AS table_reads_index_count,
  tabstat.seq_scan AS table_reads_seq_count,
  tabstat.seq_scan + tabstat.idx_scan AS table_reads_count,
  n_tup_upd + n_tup_ins + n_tup_del AS table_writes_count,
  pg_size_pretty(pg_relation_size(idstat.relid)) AS table_size
FROM
    pg_stat_user_indexes AS idstat
JOIN
    pg_indexes ON indexrelname = indexname AND
      idstat.schemaname = pg_indexes.schemaname
JOIN
    pg_stat_user_tables AS tabstat ON
      idstat.relid = tabstat.relid
WHERE
    indexdef !~* 'unique'
ORDER BY
    idstat.idx_scan DESC,
    pg_relation_size(indexrelid) DESC
                          ")
   #(reset! index-stats (.-rows %))
   #(render (r/as-element [:f> Indexes]))))


(def ^:private refresh-index-stats
  (pipe/p
   #(.query @pg-connection "SELECT pg_stat_reset()")
   #(show-index-stats)))


(defn- Indexes []
  (useInput (fn [input key]
              (if (= (s/lower-case input) "r") (show-index-stats))
              (if (and (= (s/lower-case input) "c") (.-shift key)) (refresh-index-stats))))
  (if (> (count @index-stats) 0)
    [:> Box {:flex-direction "column"}
     [:> Box {:border-style "single"}
      [:> Text
       [:> Text "Press \"R\", if you want refresh statistics"]
       [:> Newline]
       [:> Text "Press \"Shift+C\" (\"Caps Lock\" should be turned off), if you want clear statistics"]]]
     [:> Table/default {:data @index-stats}]]
    [:> Text "No info about indexes"]))
