(ns com.hdis.schema)

(def schema
  {:admission/id :uuid
   :amission [:map {:closed true}
          [:xt/id                     :admission/id]
          [:user/email                :string]
          [:user/joined-at            inst?]
          [:user/foo {:optional true} :string]
          [:user/bar {:optional true} :string]]

   :msg/id :uuid
   :msg [:map {:closed true}
         [:xt/id       :msg/id]
         [:msg/user    :user/id]
         [:msg/text    :string]
         [:msg/sent-at inst?]]})

(def module
  {:schema schema})
