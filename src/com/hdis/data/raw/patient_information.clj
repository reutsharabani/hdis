(ns com.hdis.data.raw.patient-information
  (:refer-clojure :exclude [load])
  (:require [com.hdis.data.utils :as utils]
            [com.hdis.db :as db]))

(def kind :hdis/patient-information)
(def id-field :patient-information/patient_id)
(def patient-information-object-name "patient_information.csv")

(defn process-record [patient-information-record]
  (->
   patient-information-record
   (utils/namespace-map :patient-information)))

(defn query [ctx]
  (utils/get-kind-data ctx kind))

(comment
  (first (load)))

