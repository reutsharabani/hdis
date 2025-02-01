(ns com.hdis.worker
  (:require [clojure.tools.logging :as log]
            [com.hdis.data.admissions :as admissions]
            [com.hdis.data.lab-results :as lab-results]
            [com.biffweb :as biff]
            [com.hdis.data.lab-tests :as lab-tests]
            [com.hdis.data.idle-patients :as idle-patients]
            [com.hdis.settings :as settings]
            [com.hdis.data.patient-information :as patient-information]
            [com.hdis.db :as db]))

(defn every-n-seconds [n]
  (iterate #(biff/add-seconds % n) (java.util.Date.)))

(def kind->id-field
  {:hdis/admission :admission/hospitalization_case_number
   :hdis/lab-result :lab-result/result_id
   :hdis/lab-test :lab-test/test_id
   :hdis/patient-information :patient-information/patient_id
   :hdis/idle-patient :idle-patient/patient_id})

(defn refresh-db [{:keys [biff.xtdb/node] :as ctx}]
  (def c* ctx)
  (let [admissions          (admissions/load)
        lab-results         (lab-results/load)
        lab-tests           (lab-tests/load)
        patient-information (patient-information/load)]
    (def a* admissions)
    (def lr* lab-results)
    (def lt* lab-tests)
    (def pi* patient-information)
    (log/info "refreshing db")
    (doseq [[kind records] {:hdis/admission admissions
                            :hdis/lab-result lab-results
                            :hdis/lab-test lab-tests
                            :hdis/patient-information patient-information}]
      (log/info "refreshing db with " (count records) kind " records")
      (try
        (db/insert-records ctx kind (kind->id-field kind) records)
        (catch Exception e
          (log/error "error refreshing db with " (count records) kind " records" e))))
    (log/info "db refreshed")
    (log/info "refreshing derived data")
    (doseq [[kind records] {:hdis/idle-patient (idle-patients/load ctx)}]
      (log/info "refreshing derived data with " (count records) kind " records")
      (try
        (db/insert-records ctx kind (kind->id-field kind) records)
        (catch Exception e
          (log/error "error refreshing derived data with " (count records) kind " records" e))))
    (log/info "derived data refreshed")))

(def module
  {:tasks [{:task     #'refresh-db
            ;; should be configurable
            :schedule #(every-n-seconds settings/refresh-interval-seconds)}]})
