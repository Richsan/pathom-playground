(ns pathom-playground.utils
  (:require [clj-http.client :as http.client]
            [cheshire.core :as chesire]))

(defn namespaced
  "Apply the string n to the supplied structure m as a namespace."
  [n m]
  (clojure.walk/postwalk
    (fn [x]
      (if (keyword? x)
        (keyword n (name x))
        x))
    m))

(defn http-get [url]
  (some-> (http.client/get url)
          :body
          (chesire/parse-string keyword)))


