(ns garamond.version
  (:require [clojure.string :as string]
            [clojure.tools.logging :as log])
  (:import (com.github.zafarkhaja.semver Version)))

;; cf https://github.com/zafarkhaja/jsemver

(defn parse
  [version-str]
  (try
    (Version/valueOf version-str)
    (catch Exception e
      (log/errorf "Can't parse version string from '%s': %s" version-str (.getMessage e))
      nil)))
