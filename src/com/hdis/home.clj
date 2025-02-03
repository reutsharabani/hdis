(ns com.hdis.home
  (:require [com.hdis.middleware :as mid]
            [com.biffweb :as biff]
            [com.hdis.ui :as ui]
            [clojure.tools.logging :as log]
            [com.hdis.data.derived.idle-patients :as idle-patients]
            [com.hdis.data.derived.active-patients :as active-patients]
            [com.hdis.data.utils :as utils]))

(defn class-item-css [selected?]
  (if selected?
    "text-yellow-500 hover:text-yellow-900 text-decoration-underlined"
    "text-yellow-500 hover:text-yellow-900"))

(defn headerize [text]
  (-> text
      (clojure.string/replace "-" " ")
      (clojure.string/replace "_" " ")
      clojure.string/capitalize))

(defn nav-item [ctx text path]
  [:p {:hx-get    path
       :hx-swap   "innerHTML"
       :hx-target "#table"
       :class     (class-item-css (= path (-> ctx :query-params (get "selected"))))} text])

(defn home-page [ctx]
  (let [selected (-> ctx :query-params (get "selected"))]
    (log/info "selected: " selected)
    (ui/page
     ctx
     [:div {:class "fixed top-8 left-0 text-yellow-500 bg-gray-800"}
      [:nav {:id "main" :class "bg-gray-800 space-x-10 fixed top-0 left-0 w-full items-center md:flex"}
       (nav-item ctx "Idle Patients" "/idle-patient?selected=idle-patient")
       (nav-item ctx "Active Patients" "/active-patient?selected=active-patient")
       (nav-item ctx "Admissions" "/admission?selected=admission")
       (nav-item ctx "Patient Information" "/patient-information?selected=patient-information")
       (nav-item ctx "Lab Tests" "/lab-test?selected=lab-test")
       (nav-item ctx "Lab Results" "/lab-result?selected=lab-result")]
      [:div {:id    "table"
             :class "bg-gray-800 border-2 border-solid border-yellow-500 shadow-md fixed left-0 w-full h-full"}]])))

(defn stringify-val [v]
  (cond
    (keyword? v) (name v)
    :else        (str v)))

(defn make-hx-get-path [ctx header]
  (let [sort-dir   (-> ctx :query-params (get "dir" "desc"))
        sort-field (-> ctx :query-params (get "sort-by" "id"))
        view       (-> ctx :query-params (get "selected"))
        patient-id (-> ctx :query-params (get "patient-id"))
        url (str view "?sort-by=" header "&selected=" view "&dir=" (if (= sort-field header)
                                                                     (if (= "asc" sort-dir)
                                                                       "desc"
                                                                       "asc")
                                                                     "desc"))]
    (cond-> url
      (some? patient-id) (str "&patient-id=" patient-id))))

(defn table-view [ctx records]
  (def ctx* ctx)
  (def records* records)
  (let [clean-records (map #(dissoc % :db/op :xt/id :hdis/kind) records)
        stripped-records (map utils/un-namespace-map clean-records)
        headers        (some-> stripped-records first keys sort)
        header-names   (some->> headers (map name))
        sort-field     (or (some-> ctx :query-params (get "sort-by" "id") keyword)
                           (first headers))
        sort-dir      (-> ctx :query-params (get "dir" "desc"))
        sorted-records (cond-> (sort-by (fn [r] (sort-field r)) stripped-records)
                         (= "asc" sort-dir) reverse)]
    [:div {:class "overflow-x-auto h-[95%] overflow-y-auto w-full"}
     [:table {:class "w-full"}
      [:thead
       [:tr
        (for [header header-names]
          [:th {:class     "hover:text-yellow-900 p-2"
                :hx-get    (make-hx-get-path ctx header)
                :hx-swap   "innerHTML"
                :hx-target "#table"} (headerize header)])]]
      [:tbody {:class "h-full"}
       (for [record sorted-records]
         [:tr (cond-> {:class "hover:text-yellow-100 border-dashed border-2 border-gray-500"}
                (contains? record :patient_id) (assoc :hx-get (str "/focus-patient?selected=focus-patient&patient-id=" (:patient_id record))
                                                      :hx-swap "innerHTML"
                                                      :hx-target "#table"))
          (for [header headers]
            [:td {:class "items-center text-center py-2"} (stringify-val (header record))])])]]]))

(defn make-table [ctx kind]
  (let [kind-records (utils/get-kind-data ctx kind)]
    (table-view ctx kind-records)))

(defn patient-information-table [ctx]
  (make-table ctx :hdis/patient-information))

(defn lab-tests-table [ctx]
  (make-table ctx :hdis/lab-test))

(defn lab-results-table [ctx]
  (make-table ctx :hdis/lab-result))

(defn admissions-table [ctx]
  (make-table ctx :hdis/admission))

(defn idle-patients-table [ctx]
  ;;(table-view ctx (idle-patient/query ctx)))
  (table-view ctx (idle-patients/query ctx)))

(defn active-patients-table [ctx]
  ;;(table-view ctx (idle-patient/query ctx)))
  (table-view ctx (active-patients/query ctx)))

(defn focus-patient-table [ctx]
  (def ctx* ctx)
  (let [patient-id (-> ctx :query-params (get "patient-id"))
        lab-tests (biff/q (:biff/db ctx) '{:find  (pull ?lab-test [:lab-test/order_time
                                                                   :lab-test/test_id
                                                                   :lab-test/test_name])
                                           :in [patient-id]
                                           :where [[?lab-test :lab-test/patient_id patient-id]
                                                   [?lab-test :lab-test/test_id ?lab-test-id]]}
                          patient-id)
        lab-results (->>
                     (biff/q (:biff/db ctx) '{:find  (pull ?lab-results [:lab-result/result_value
                                                                         :lab-result/performed_time
                                                                         :lab-result/test_id])
                                              :in [lab-test-ids]
                                              :where [[?lab-results :lab-result/test_id ?lab-test-id]
                                                      [(contains? lab-test-ids ?lab-test-id)]]}
                             (into #{} (map :lab-test/test_id lab-tests))))
        lab-results-by-test-id (into {} (for [lab-result lab-results]
                                          [(:lab-result/test_id lab-result) lab-result]))
        empty-lab-result {:lab-result/result_value "N/A"
                          :lab-result/performed_time "N/A"}
        records (for [lab-test lab-tests]
                  (let [lab-result (get lab-results-by-test-id (:lab-test/test_id lab-test) empty-lab-result)]
                    (merge lab-test lab-result)))]
    (def patient-id* patient-id)
    (def lab-tests* lab-tests)
    (def lab-results* lab-results)
    (def lab-results-by-test-id* lab-results-by-test-id)
    (def records* records)
    [:h1 {:class "overflow-x-auto h-[95%] overflow-y-auto w-full"} ;
     (str "Patient " patient-id)
     (table-view ctx records)]))

(def module
  {:routes [["" {:middleware [mid/wrap-redirect-signed-in]}
             ["/" {:get home-page}]]
            ["/focus-patient"       {:get focus-patient-table}]
            ["/idle-patient"        {:get idle-patients-table}]
            ["/active-patient"      {:get active-patients-table}]
            ["/patient-information" {:get patient-information-table}]
            ["/lab-test"            {:get lab-tests-table}]
            ["/lab-result"          {:get lab-results-table}]
            ["/admission"           {:get admissions-table}]]})
