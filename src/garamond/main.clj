(ns garamond.main
  (:require [clojure.tools.cli :as cli]
            [clojure.tools.deps.alpha.gen.pom :as pom]
            [clojure.string :as string]
            [clojure.tools.logging :as log]
            [garamond.git :as git]
            [garamond.version :as version]))


(def cli-options
  [["-h" "--help"]
   ["-p" "--prefix PREFIX" :default "v"]])

(defn usage [options-summary]
  (->> ["garamond is a utility for printing and incrementing versions based on git tags."
        ""
        "Usage: clojure -m garamond.main [options] action [args...]"
        ""
        "Options:"
        options-summary
        ""
        "Actions:"
        "  version              Print the current version number of this repository"
        "  increment inc-type   Increment the version"
        ""
        "Increment types:"
        "  major              Print the current version number of this repository"
        "Please refer to the manual page for more information."]
       (string/join \newline)))

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (string/join \newline errors)))

(defn validate-args
  "Validate command line arguments. Either return a map indicating the program
  should exit (with a error message, and optional ok status), or a map
  indicating the action the program should take and the options provided."
  [args]
  (let [{:keys [options arguments errors summary]} (cli/parse-opts args cli-options)
        action (some-> arguments first keyword)]
    (cond
      (:help options) ; help => exit OK with usage summary
      {:exit-message (usage summary) :ok? true}
      errors ; errors => exit with description of errors
      {:exit-message (error-msg errors)}
      ;; custom validation on arguments
      (and (>= (count arguments) 1)
           (#{:version :increment :status} action))
      {:action action :args (rest arguments) :options (assoc options :summary summary)}
      :else ; failed custom validation => exit with usage summary
      {:exit-message (usage summary)})))

(defn exit
  ([]
   (exit 0))
  ([code]
   (exit code nil))
  ([code message]
   (throw (ex-info "Exit condition" {:code code :message message}))))

(defn print-version [options status]
  (println (version/to-string (:version status) options)))

(def valid-inc-types #{:major :minor :patch :minor-rc :minor-release :major-rc :major-release})

(defn increment [options args {:keys [version] :as status}]
  (let [inc-type (some-> args first keyword)]
    (when-not inc-type
      (exit 1 (usage (:summary options))))
    (when-not (contains? valid-inc-types inc-type)
      (exit 1 (format "Unknown increment type '%s'! Valid types: %s."
                      (name inc-type) (->> valid-inc-types (map name) sort (string/join ", ")))))
    (let [new-version (version/increment version inc-type)]
      (println (format "%s increment of %s -> %s" (name inc-type)
                       (version/to-string version options)
                       (version/to-string new-version options))))))

(defn -main [& args]
  (try
    (let [{:keys [action options args exit-message ok?]} (validate-args args)
          status (git/current-status)]
      (when exit-message
        (exit (if ok? 0 1) exit-message))
      (case action
        :version   (print-version options status)
        :increment (increment options args status)
        :pom       (println "hmm")))
    (catch Exception e
      (let [{:keys [code message]} (ex-data e)]
        (if message
          (println message)
          (when-not code ; exception
            (log/error e)))
        (System/exit (or code 128)))))
  (System/exit 0))
