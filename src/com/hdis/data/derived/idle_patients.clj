(ns com.hdis.data.derived.idle-patients
  (:refer-clojure :exclude [load])
  (:require
   [clojure.tools.logging :as log]
   [xtdb.api :as xt]
   [clojure.set :as set]
   [com.hdis.data.utils :as utils]
   [com.biffweb :as biff :refer [q]]
   [com.hdis.data.derived.active-patients :as active-patients]))

(defn epoch-hours-ago [hours-ago]
  (java.time.Instant/ofEpochMilli (- (.getTime (java.util.Date.)) (* hours-ago 60 60 1000))))

(defn query
  "patients who have been idle for more than 48 hours"
  [ctx]
  (let [active-admissions (q (:biff/db ctx) '{:find  (pull ?a [:admission/hospitalization_case_number])
                                              :in    [cutoff]
                                              :where [[?a :admission/release_date "NULL"]
                                                      [?a :admission/patient_id ?p]
                                                      [?lt :lab-test/patient_id ?p]
                                                      [?lt :lab-test/order-time ?ot]
                              ;; order-time is over two days ago
                                                      [(> cutoff ?ot)]]}

                             (epoch-hours-ago 48))
        acns (set (map :admission/hospitalization_case_number active-admissions))
        idle-admissions (q (:biff/db ctx) '{:find  (pull ?a [*])
                                            :in    [acns]
                                            :where [[?a :admission/release_date "NULL"]
                                                    [?a :admission/hospitalization_case_number ?acn]
                                                    (not [(contains? acns ?acn)])]}
                           acns)]
    (def acns* acns)
    (def active-admissions* active-admissions)
    (def idle-admissions* idle-admissions)
    idle-admissions))

(comment
  (query ctx*)
  (map :admission/hospitalization_case_number (query-2 ctx*)))
