(ns com.hdis.data.utils
  (:require [com.hdis.data.s3 :as s3]
            [clojure.tools.logging :as log]
            [clj-http.client :as clj-http]))

(def datetime-formatter (java.time.format.DateTimeFormatter/ofPattern "M/d/yyyy h:mm:ss a" java.util.Locale/ENGLISH))

(defn date+time->timestamp ^java.time.LocalDateTime [date time]
  (let [time-str (str date " " time)]
    (java.time.LocalDateTime/parse time-str datetime-formatter)))

(def path->etag (atom {}))

(defn load-if-needed [path]
  (let [object-meta   (clj-http.client/head (str s3/bucket-url "/" path))
        current-etag  (-> object-meta :headers (get "ETag"))
        needs-reload? (not= (get @path->etag path) current-etag)]
    (when-not needs-reload?
      (log/info (str "No need to reload " path)))
    (when needs-reload?
      (log/info (str "Reloading " path " new etag [" current-etag "] old was [" (get @path->etag path "none") "]"))
      (swap! path->etag assoc path current-etag)
      (s3/load-csv-object s3/bucket-url path))))

(defn namespace-map [record kind]
  (update-keys record (comp keyword (partial str (name kind) "/") name)))

(defn un-namespace-map [record]
  (update-keys record (comp keyword name)))

(defn re-namespace-map [record kind]
  (-> record
      (un-namespace-map)
      (namespace-map kind)))
