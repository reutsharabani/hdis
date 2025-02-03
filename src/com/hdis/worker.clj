(ns com.hdis.worker
  (:require [clojure.tools.logging :as log]
            [com.biffweb :as biff]
            [com.hdis.data.utils :as utils]
            [com.hdis.data.raw.admissions :as admissions]
            [com.hdis.data.raw.lab-results :as lab-results]
            [com.hdis.data.raw.lab-tests :as lab-tests]
            [com.hdis.data.raw.patient-information :as patient-information]
            [com.hdis.settings :as settings]
            [com.hdis.db :as db])
  (:import [java.time Instant]))

(defn every-n-seconds [n]
  (iterate #(biff/add-seconds % n) (java.util.Date.)))

(def kind->id-field
  {:hdis/admission :admission/hospitalization_case_number
   :hdis/lab-result :lab-result/result_id
   :hdis/lab-test :lab-test/test_id
   :hdis/patient-information :patient-information/patient_id
   :hdis/idle-patient :idle-patient/patient_id
   :hdis/unreleased-patient :unreleased-patient/admission-id})

(defn refresh-raw-data [ctx]
  (log/info "refreshing raw data")
  (def c* ctx)
  (utils/load-if-needed ctx
                        admissions/s3-object-name
                        admissions/kind
                        admissions/id-field
                        admissions/process-record)
  (utils/load-if-needed ctx
                        lab-results/lab-results-object-name
                        lab-results/kind
                        lab-results/id-field
                        lab-results/process-record)
  (utils/load-if-needed ctx
                        lab-tests/lab-tests-object-name
                        lab-tests/kind
                        lab-tests/id-field
                        lab-tests/process-record)
  (utils/load-if-needed ctx
                        patient-information/patient-information-object-name
                        patient-information/kind
                        patient-information/id-field
                        patient-information/process-record)
  (log/info "raw data refreshed"))

(def module
  {:tasks [{:task     #'refresh-raw-data
            ;; should be configurable
            :schedule #(every-n-seconds settings/derived-data-refresh-interval-seconds)}]})

(comment
  ;; some dummy data
  (let [ctx c*
        dummy-admissions [;; patient with no tests at all
                          {:admission/admission-time (java.time.Instant/parse "2024-03-04T04:13:00Z")
                           :admission/hospitalization_case_number "1"
                           :admission/patient_id "1"
                           :admission/department "Radiology"
                           :admission/release_time "NULL"
                           :admission/room_number "723B"
                           :admission/release_date "NULL"
                           :hdis/kind :hdis/admission}
                          ;; patient recent tests
                          {:admission/admission-time (java.time.Instant/parse "2024-03-04T04:13:00Z")
                           :admission/hospitalization_case_number "2"
                           :admission/patient_id "2"
                           :admission/department "Radiology"
                           :admission/release_time "NULL"
                           :admission/room_number "723B"
                           :admission/release_date "NULL"
                           :hdis/kind :hdis/admission}
                          ]

        dummy-lab-tests [{:lab-test/test_id "2"
                          :lab-test/patient_id "2"
                          :lab-test/test_name "D-Dimer"
                          :lab-test/ordering_physician "Dr. Thomas Martinez"
                          :lab-test/order-time (Instant/now)
                          :db/op :upsert
                          :hdis/kind :hdis/lab-test
                          :xt/id "lab-test-2"}]
        dummy-lab-results [{:lab-result/test_id "3"
                            :lab-result/result_id "3"
                            :lab-result/result_value 0.5
                            :lab-result/performed_time (Instant/parse "2017-11-05T12:18:00Z")
                            :db/op :upsert
                            :hdis/kind :hdis/lab-result
                            :xt/id "lab-result-3"}]]

    (comment
      (biff/q (:biff/db ctx) '{:find  (pull ?r [*])
                               :in    [kind]
                               :where [[?r :hdis/kind kind]]}
              kind))
    (db/insert-records ctx admissions/kind admissions/id-field dummy-admissions)
    (db/insert-records ctx lab-tests/kind lab-tests/id-field dummy-lab-tests)
    (db/insert-records ctx lab-results/kind lab-results/id-field dummy-lab-results)))
