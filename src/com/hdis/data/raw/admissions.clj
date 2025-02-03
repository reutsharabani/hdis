(ns com.hdis.data.raw.admissions
  (:refer-clojure :exclude [load])
  (:require [com.hdis.data.utils :as utils]
            [com.hdis.db :as db])
  (:import [java.time Instant]))

(def kind :hdis/admission)
(def id-field :admission/hospitalization_case_number)

(def s3-object-name "admissions.csv")

(defn process-record [record]
  (-> record
      (dissoc :admission_date :admission_time)
      (assoc :admission-time (let [d (:admission_date record)
                                   t (:admission_time record)]
                               (utils/date+time->timestamp d t)))
      (utils/namespace-map :admission)))

(defn query [ctx]
  (utils/get-kind-data ctx kind))
