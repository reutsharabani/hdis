(ns com.hdis.data.s3
  (:require [clojure.string :as s]
            [charred.api :as charred]))

(def bucket-url "https://external-take-home-test-wild-launch.s3.eu-west-1.amazonaws.com")

(defn load-csv-object [bucket-url object-name]
  (let [url (str bucket-url "/" object-name)
        [raw-headers & data] (-> url
                                 slurp
                                 charred/read-csv)
        headers (map keyword raw-headers)]
    (for [datum data]
      (zipmap headers datum))))
