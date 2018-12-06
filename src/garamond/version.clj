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

(defn to-string [version {:keys [prefix]}]
  (str prefix version))

(defn version-with-rc0 [version]
  (some-> version (str "-rc.0") parse))

(defn version-without-rc [version]
  (some-> version str (string/replace #"-rc\.\d+" "") parse))

(defmulti increment (fn [_ inc-type] inc-type))

(defmethod increment :default [_ inc-type]
  (log/errorf "Unknown increment type %s!" inc-type)
  nil)

(defmethod increment :major [^Version version _]
  (some-> version .incrementMajorVersion))

(defmethod increment :minor [^Version version _]
  (some-> version .incrementMinorVersion))

(defmethod increment :patch [^Version version _]
  (some-> version .incrementPatchVersion))

(defmethod increment :major-rc [^Version version _]
  (let [current-rc (.getPreReleaseVersion version)]
    (if (string/blank? current-rc)
      (-> version .incrementMajorVersion version-with-rc0)
      (some-> version .incrementPreReleaseVersion))))

(defmethod increment :minor-rc [^Version version _]
  (let [current-rc (.getPreReleaseVersion version)]
    (if (string/blank? current-rc)
      (-> version .incrementMinorVersion version-with-rc0)
      (some-> version .incrementPreReleaseVersion))))

(defmethod increment :major-release [^Version version _]
  (let [current-rc (.getPreReleaseVersion version)]
    (if (string/blank? current-rc)
      (some-> version .incrementMajorVersion)
      (version-without-rc version))))

(defmethod increment :minor-release [^Version version _]
  (let [current-rc (.getPreReleaseVersion version)]
    (if (string/blank? current-rc)
      (some-> version .incrementMinorVersion)
      (version-without-rc version))))
