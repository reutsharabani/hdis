(ns com.hdis.home
  (:require [com.hdis.middleware :as mid]
            [com.biffweb :as biff]
            [com.hdis.ui :as ui]
            [clojure.tools.logging :as log]
            [xtdb.api :as xt]))

(defn class-item-css [selected?]
  (if selected?
    "text-yellow-500 hover:text-yellow-900 text-decoration-underlined"
    "text-yellow-500 hover:text-yellow-900"))

(defn headerize [text]
  (-> text
       (clojure.string/replace "-" " ")
       (clojure.string/replace "_" " ")
       clojure.string/capitalize))

(defn home-page [ctx]
  (let [selected (-> ctx :query-params (get "selected"))]
    (log/info "selected: " selected)
    (ui/page
     ctx
     [:div {:class "fixed top-8 left-0 text-yellow-500 bg-gray-800"}
      [:nav {:id "main" :class "bg-gray-800 space-x-10 fixed top-0 left-0 w-full items-center md:flex"}
       [:p {:hx-get "/idle-patients?selected=idle-patients"
            :hx-swap "innerHTML"
            :hx-target "#table"
            :class (class-item-css (= selected "idle-patients"))} "Idle Patients"]
       [:p {:hx-get "/admission?selected=admission"
            :hx-swap "innerHTML"
            :hx-target "#table"
            :class (class-item-css (= selected "admission"))} "Admissions"]
       [:p {:hx-get "/patient-information"
            :hx-swap "innerHTML"
            :hx-target "#table"
            :class (class-item-css (= selected "partient-information"))} "Patient Information"]
       [:p {:hx-get "/lab-test"
            :hx-swap "innerHTML"
            :hx-target "#table"
            :class (class-item-css (= selected "lab-test"))} "Lab Tests"]
       [:p {:hx-get "/lab-result"
            :hx-swap "innerHTML"
            :hx-target "#table"
            :class (class-item-css (= selected "lab-result"))} "Lab Results"]]
      [:div {:id "table"
             :class "bg-gray-800 border-2 border-solid border-yellow-500 shadow-md fixed left-0 w-full h-full"}]])))

(defn stringify-val [v]
  (cond
    (keyword? v) (name v)
    :else (str v)))

(defn make-hx-get-path [kind header sort-field sort-dir]
  (str (name kind) "?sort-by=" header "&selected=" (name kind) "&dir=" (if (= sort-field header)
                                                                         (if (= "asc" sort-dir)
                                                                           "desc"
                                                                           "asc")
                                                                         "desc")))

(defn make-table [ctx kind]
  (def c* ctx)
  (xt/q (:biff/db c*) '{:find [(pull ?r [*])]
                        :where [[?r :hdis/kind :hdis/admission]]})
  (let [raw-records (xt/q (:biff/db ctx) '{:find [(pull ?r [*])]
                                           :in [kind]
                                           :where [[?r :hdis/kind kind]]}
                          kind)
        records (into [] cat raw-records)
        sort-dir (get (:query-params ctx) "dir" "asc")
        sort-field (get (:query-params ctx) "sort-by" "id")
        sort-fn (keyword sort-field)
        sorted-records (cond-> (sort-by sort-fn records)
                         (= sort-dir "desc") reverse)
        headers (some-> sorted-records first (dissoc :db/op :xt/id :hdis/kind) keys sort)
        header-names (some->> headers (map name))]
    (def r* sorted-records)
    (def s* sort-fn)
    (def h* headers)
    [:table {:class "overflow-scroll w-full"}
     [:thead
      [:tr
       (for [header header-names]
         [:th {:class "hover:text-yellow-900 p-4"
               :hx-get (make-hx-get-path kind header sort-field sort-dir)
               :hx-swap "innerHTML"
               :hx-target "#table"} (headerize header)])]]
     [:tbody
      (for [record sorted-records]
        [:tr {:class "hover:text-yellow-100"}
         (for [header headers]
           [:td {:class "items-center text-center"} (stringify-val (header record))])])]]))

(defn patient-information-table [ctx]
  (make-table ctx :hdis/patient-information))

(defn lab-tests-table [ctx]
  (make-table ctx :hdis/lab-test))

(defn lab-results-table [ctx]
  (make-table ctx :hdis/lab-result))

(defn admissions-table [ctx]
  (make-table ctx :hdis/admission))

(defn idle-patients-table [ctx]
  (make-table ctx :hdis/idle-patient))

(def module
  {:routes [["" {:middleware [mid/wrap-redirect-signed-in]}
             ["/"                  {:get home-page}]]
            ["/idle-patients" {:get idle-patients-table}]
            ["/patient-information" {:get patient-information-table}]
            ["/lab-test"           {:get lab-tests-table}]
            ["/lab-result"         {:get lab-results-table}]
            ["/admission"          {:get admissions-table}]]})
