(ns garamond.git
  (:require [cuddlefish.core :as cuddlefish]))

(defn current []
  (cuddlefish/status {:git "git"}))
