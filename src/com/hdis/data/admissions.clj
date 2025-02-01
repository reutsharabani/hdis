(ns com.hdis.data.admissions
  (:refer-clojure :exclude [load])
  (:require [com.hdis.data.utils :as utils]))

(def admissions-object-name "admissions.csv")
(def last-processed-etag (atom nil))

(defn- process-admission-record [record]
  (-> record
      (dissoc :admission_date :admission_time)
      (assoc :admission-time (let [d (:admission_date record)
                                   t (:admission_time record)]
                               (utils/date+time->timestamp d t)))
      (utils/namespace-map :admission)))

(defn load []
  (->> admissions-object-name
       utils/load-if-needed
       (map process-admission-record)))

(comment
  (first (load))

  (first (load)))
