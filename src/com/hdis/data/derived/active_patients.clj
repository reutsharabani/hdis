(ns com.hdis.data.derived.active-patients
  (:refer-clojure :exclude [load])
  (:require
   [com.biffweb :as biff :refer [q]]))

(defn epoch-hours-ago [hours-ago]
  (java.time.Instant/ofEpochMilli (- (.getTime (java.util.Date.)) (* hours-ago 60 60 1000))))

(defn query
  "patients who have been idle for more than 48 hours"
  [ctx]
  (def ctx* ctx)
  (q (:biff/db ctx) '{:find  (pull ?a [*])
                      :in    [cutoff]
                      :where [[?a :admission/release_date "NULL"]
                              [?a :admission/patient_id ?p]
                              [?lt :lab-test/patient_id ?p]
                              [?lt :lab-test/order-time ?ot]
                              ;; order-time is over two days ago
                              [(< cutoff ?ot)]]}

     (epoch-hours-ago 48)))

(comment
  (query ctx*)
  (map :admission/hospitalization_case_number (query-2 ctx*)))
