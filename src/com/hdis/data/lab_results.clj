(ns com.hdis.data.lab-results
  (:refer-clojure :exclude [load])
  (:require [com.hdis.data.utils :as utils]))

(def lab-results-object-name "lab_results.csv")

(defn- process-lab-result-record [lab-results-record]
  (->
   lab-results-record
   (dissoc :performed_date :performed_time)
   (assoc :result_value (Double/parseDouble (:result_value lab-results-record)))
   (assoc :performed_time (let [performed-time-str (str (:performed_date lab-results-record) " " (:performed_time lab-results-record))]
                            (java.time.LocalDateTime/parse performed-time-str utils/datetime-formatter)))
   (utils/namespace-map :lab-result)))

(defn load []
  (->> lab-results-object-name
       utils/load-if-needed
       (map process-lab-result-record)))

(comment
  (first (load)))
