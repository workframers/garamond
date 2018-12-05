(ns garamond.version
  (:require [clojure.string :as string])
  (:import (com.github.zafarkhaja.semver Version)))

;; cf https://github.com/kherge/java.semver

(defn parse
  ([v-str]
   (parse v-str "v"))
  ([v-str prefix]
   (let [ver (string/replace v-str #"^[^\d]*" "")]
     (bean (Version/valueOf ver)))))
