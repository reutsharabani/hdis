(ns com.hdis.data.patient-information
  (:refer-clojure :exclude [load])
  (:require [com.hdis.data.utils :as utils]))

(def patient-information-object-name "patient_information.csv")

(defn- process-patient-information-record [patient-information-record]
  (->
   patient-information-record
   (utils/namespace-map :patient-information)))

(defn load []
  (->> patient-information-object-name
       utils/load-if-needed
       (map process-patient-information-record)))

(comment
  (first (load)))

