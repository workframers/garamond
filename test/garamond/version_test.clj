(ns garamond.version-test
  (:require [clojure.test :refer :all]
            [garamond.version :as ver :refer [increment]])
  (:import (com.github.zafarkhaja.semver Version)))

(defn v [s] (Version/valueOf s))

(deftest inc-test
  (testing "Basics"
    (is (= (v "1.2.4")
           (increment (v "1.2.3") :patch))
        "inc patch")
    (is (= (v "1.3.0")
           (increment (v "1.2.3") :minor))
        "inc minor")
    (is (= (v "2.0.0")
           (increment (v "1.2.3") :major))
        "inc major"))
  (testing "RC / release"
    (is (= (v "2.0.0-rc.0")
           (increment (v "1.2.3") :rc))
        "first rc")
    (is (= (v "2.0.0-rc.3")
           (increment (v "2.0.0-rc.2") :rc))
        "subsequent rc")
    (is (= (v "2.0.0")
           (increment (v "2.0.0-rc.6") :release))
        "release, from rc")
    (is (= (v "3.0.0")
           (increment (v "2.0.0") :release))
        "release, no rc")))
