(ns garamond.pom
  (:require [taoensso.timbre :as timbre]
            [garamond.version :as version]
            [clojure.tools.deps.alpha.gen.pom :as pom]))

(defn generate!
  ""
  [version options]
  (timbre/debugf "Generating pom.xml for %s..." (version/to-string version options)))
