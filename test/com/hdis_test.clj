(ns com.hdis-test
  (:require [clojure.test :as t]
            [com.biffweb :as biff :refer [test-xtdb-node]]
            [com.hdis.data.derived.idle-patients :as idle-patients]
            [com.hdis.data.raw.admissions :as admissions]
            [com.hdis.data.raw.patient-information :as patient-information]
            [com.hdis.data.raw.lab-results :as lab-results]
            [com.hdis.data.raw.lab-tests :as lab-tests]
            [com.hdis :as main]
            [xtdb.api :as xt])
  (:import [java.time Instant]))

(def dummy-admissions (fn [] [{:admission/patient_id "1337"
                               :admission/hospitalization_case_number "1337"
                               :admission/release_date "NULL"
                               :admission/release_time "NULL"
                               :admission/department "Cardiology"
                               :admission/room_number "305A"
                               :admission/admission-time (Instant/parse "2022-03-20T03:20:00Z")}]))

(t/deftest test-idle-patients
  (when false
    (t/is (= 1 1)))
  )
