(ns garamond.git
  (:require [cuddlefish.core :as cuddlefish]
            [clojure.java.shell :as shell]
            [garamond.util :as u]
            [garamond.version :as v]
            [taoensso.timbre :as log]
            [clojure.string :as string]))

(def cf-config
  "The default configuration values."
  {:git              "git"
   :describe-pattern cuddlefish/git-describe-pattern})

(defn- handle-error [cmd-line {:keys [exit out err]}]
  (log/errorf "Command '%s' returned %d!" (string/join " " cmd-line) exit)
  (when-not (string/blank? out)
    (log/errorf "stdout: %s" (string/trim out)))
  (when-not (string/blank? err)
    (log/errorf "stderr: %s" (string/trim err)))
  (u/exit exit))

(defn run-git-command
  ([cmd-line]
   (run-git-command cmd-line nil))
  ([cmd-line options]
   (let [full-cmd (into [(:git cf-config)] cmd-line)
         _        (log/trace (string/join " " full-cmd))
         result   (apply shell/sh full-cmd)
         stdout   (some-> result :out string/trim-newline)]
     (when-not (zero? (:exit result))
       (handle-error full-cmd result))
     (when-not (string/blank? stdout)
       (log/debugf "Output of '%s': %s" (string/join " " full-cmd) stdout))
     stdout)))

(defn current-status []
  (let [status  (cuddlefish/status cf-config)
        current (:tag status)
        [_ prefix ver-str] (re-matches #"^(\D*)(.*)$" current)
        version (v/parse ver-str)]
    {:version version :git status :current current :prefix prefix}))

(defn commit-message [{:keys [message incr-type]}]
  (if message
    message
    (format "Automatically-generated tag from garamond%s"
            (if incr-type (str " " (name incr-type)) ""))))

(defn current-sha []
  (run-git-command ["log" "-1" "--format=%H"]))

(defn remote-url []
  (run-git-command ["remote" "get-url" "origin"]))

(defn tag!
  "Create a new git tag based on the passed-in version. Abort (throw an exception)"
  [version options status]
  (let [new-ver  (v/to-string version options)
        cmd-line ["tag" "--annotate" "--message" (commit-message options) new-ver]]
    (run-git-command cmd-line options)))
