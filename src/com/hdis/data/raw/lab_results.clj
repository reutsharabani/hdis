(ns com.hdis.data.raw.lab-results
  (:refer-clojure :exclude [load])
  (:require [com.hdis.data.utils :as utils]
            [com.hdis.db :as db]))

(def kind :hdis/lab-result)
(def id-field :lab-result/result_id)
(def lab-results-object-name "lab_results.csv")

(defn process-record [lab-results-record]
  (->
   lab-results-record
   (dissoc :performed_date :performed_time)
   (assoc :result_value (Double/parseDouble (:result_value lab-results-record)))
   (assoc :performed_time (let [performed-time-str (str (:performed_date lab-results-record) " " (:performed_time lab-results-record))]
                            (java.time.LocalDateTime/parse performed-time-str utils/datetime-formatter)))
   (utils/namespace-map :lab-result)))

(defn query [ctx]
  (utils/get-kind-data ctx kind))
