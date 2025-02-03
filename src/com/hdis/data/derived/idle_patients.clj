(ns com.hdis.data.derived.idle-patients
  (:refer-clojure :exclude [load])
  (:require
   [com.biffweb :as biff :refer [q]]
   [com.hdis.data.derived.active-patients :as active-patients]
   ))

(defn epoch-hours-ago [hours-ago]
  (java.time.Instant/ofEpochMilli (- (.getTime (java.util.Date.)) (* hours-ago 60 60 1000))))

(defn query
  "patients who have been idle for more than 48 hours"
  [ctx]
  (let [active-admissions (active-patients/query ctx)
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
