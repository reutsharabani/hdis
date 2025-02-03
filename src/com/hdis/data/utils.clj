(ns com.hdis.data.utils
  (:require [com.hdis.data.s3 :as s3]
            [clojure.tools.logging :as log]
            [com.biffweb :as biff]
            [com.hdis.db :as db]
            [clj-http.client :as clj-http]))

(def datetime-formatter (java.time.format.DateTimeFormatter/ofPattern "M/d/yyyy h:mm:ss a" java.util.Locale/ENGLISH))

(defn date+time->timestamp ^java.time.Instant [date time]
  (let [time-str (str date " " time)
        local-date-time (java.time.LocalDateTime/parse time-str datetime-formatter)
        zone-id (java.time.ZoneId/systemDefault)]
    (.toInstant (.atZone local-date-time zone-id))))

(def path->etag (atom {}))

(defn load-if-needed [ctx path kind id-field process-fn & {:keys [force?]
                                                           :or {force? false}}]
  (let [object-meta   (clj-http.client/head (str s3/bucket-url "/" path))
        current-etag  (-> object-meta :headers (get "ETag"))
        needs-reload? (or force? (not= (get @path->etag path) current-etag))
        records (if-not needs-reload?
                  (log/info (str "No need to reload " path))
                  (do
                    (log/info (str "Reloading " path " new etag [" current-etag "] old was [" (get @path->etag path "none") "]"))
                    (swap! path->etag assoc path current-etag)
                    (s3/load-csv-object s3/bucket-url path)))]
    (->> records
         (map process-fn)
         (db/insert-records ctx kind id-field))))

(defn namespace-map [record kind]
  (update-keys record (comp keyword (partial str (name kind) "/") name)))

(defn un-namespace-map [record]
  (update-keys record (comp keyword name)))

(defn re-namespace-map [record kind]
  (-> record
      (un-namespace-map)
      (namespace-map kind)))

(defn get-kind-data [ctx kind]
  (biff/q (:biff/db ctx) '{:find  (pull ?r [*])
                           :in    [kind]
                           :where [[?r :hdis/kind kind]]}
          kind))
