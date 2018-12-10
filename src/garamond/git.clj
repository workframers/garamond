(ns garamond.git
  (:require [cuddlefish.core :as cuddlefish]
            [garamond.version :as v]
            [taoensso.timbre :as log]))

(def default-config
  "The default configuration values."
  {:git              "git"
   :describe-pattern cuddlefish/git-describe-pattern})

(defn current-status []
  (let [status  (cuddlefish/status default-config)
        current (:tag status)
        [_ prefix ver-str] (re-matches #"^(\D*)(.*)$" current)
        version (v/parse ver-str)]
    {:version version :git status :current current :prefix prefix}))

(defn tag!
  ""
  [version options]
  (let [new-ver (v/to-string version options)]
    (log/infof "Created new git tag %s" new-ver)))
