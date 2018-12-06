(ns garamond.git
  (:require [cuddlefish.core :as cuddlefish]
            [garamond.version :as v]
            [clojure.string :as string]))

(def default-config
  "The default configuration values."
  {:git              "git"
   :describe-pattern cuddlefish/git-describe-pattern})

(defn current []
  (let [status (cuddlefish/status default-config)
        ver-str (string/replace (:tag status) #"^\D*" "")
        version (v/parse ver-str)]
    {:version version :git status :version-str (str version)}))
