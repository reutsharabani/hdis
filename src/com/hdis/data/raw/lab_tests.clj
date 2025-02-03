(ns com.hdis.data.raw.lab-tests
  (:refer-clojure :exclude [load])
  (:require [com.hdis.data.utils :as utils]
            [com.hdis.db :as db]))

(def kind :hdis/lab-test)
(def id-field :lab-test/test_id)

(def lab-tests-object-name "lab_tests.csv")

(defn process-record [lab-tests-record]
  (-> lab-tests-record
      (dissoc :order_date :order_time)
      (assoc :order-time (let [d (:order_date lab-tests-record)
                               t (:order_time lab-tests-record)]
                           (utils/date+time->timestamp d t)))
      (utils/namespace-map :lab-test)))

(defn query [ctx]
  (utils/get-kind-data ctx kind))

(comment
  {:test_id "24117", :patient_id "7168386", :test_name "Cortisol", :order_date "10/11/2017", :order_time "10:30:00 AM", :ordering_physician "Dr. Mia Patel"}
  )
