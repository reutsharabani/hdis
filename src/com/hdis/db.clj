(ns com.hdis.db
  (:require
   [xtdb.api :as xt]))

(defn xtdbize [kind id-field record]
  (def record* record)
  (def kind* kind)
  (def id-field* id-field)
  (comment
    (str (name kind*) "-" (id-field* record*)))

  (merge record {:xt/id     (str (name kind) "-" (id-field record))
                 :db/op     :upsert
                 :hdis/kind kind}))

(defn insert-record [{:keys [biff.xtdb/node]} kind id-field record]
  (xt/submit-tx node [[::xt/put (xtdbize kind id-field record)]]))

(defn insert-records [ctx kind id-field records]
  (doseq [record records]
    (insert-record ctx kind id-field record)))

(defn read-records [ctx kind]
  (let [node (:biff.xtdb/node ctx)]
    (xt/q node {:find  '[*]
                :where [[:hdis/kind kind]]})))
