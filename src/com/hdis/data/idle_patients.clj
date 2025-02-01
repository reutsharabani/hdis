(ns com.hdis.data.idle-patients
  (:refer-clojure :exclude [load])
  (:require
   [xtdb.api :as xt]
   [com.hdis.data.utils :as utils]
   [com.biffweb :as biff :refer [q]]))

(defn calculate-idle-patients [ctx]
  (q (:biff/db ctx) '{:find  (pull ?admission [*])
                      :where [[?admission :admission/release_date "NULL"]
                              [?admission :admission/patient_id ?patient-id]
                              [?lab-test :lab-test/patient_id ?patient-id]
                              [?lab-test :lab-test/test_id ?test-id]
                              (not [?lab-result :lab-result/test_id ?test-id])]}))

(defn delete-idle-patients [ctx]
  (let [node (:biff.xtdb/node ctx)
        db   (:biff/db ctx)
        eids (q db
                '{:find  [?e]
                  :where [[?e :kind/idle-patient]]})]
    (xt/submit-tx node
                  (mapv (fn [[eid]] [:xtdb.api/delete eid]) eids))))

(defn load [ctx]
  (def ctx* ctx)
  (delete-idle-patients ctx)
  (for [idle-patient (calculate-idle-patients ctx)]
    (-> idle-patient
        (utils/re-namespace-map :idle-patient)
        (assoc :hdis/kind :hdis/idle-patients))))

(comment
  (def res*
    (q (:biff/db ctx*) '{:find  (pull ?admission [*])
                         :where [[?admission :admission/release_date "NULL"]
                                 [?admission :admission/patient_id ?patient-id]
                                 [?lab-test :lab-test/patient_id ?patient-id]
                                 [?lab-test :lab-test/test_id ?test-id]
                                 (not [?lab-result :lab-result/test_id ?test-id])]}))
  (count res*))
