(ns garamond.main
  (:require [garamond.git :as git]
            [clojure.tools.cli :as cli]
            [clojure.tools.deps.alpha.gen.pom :as pom]
            [clojure.string :as string]
            [clojure.tools.logging :as log]))

(def cli-options
  [["-h" "--help"]
   ["-p" "--prefix" :default 0]])

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
  (let [{:keys [options arguments errors summary]} (cli/parse-opts args cli-options)]
    (cond
      (:help options) ; help => exit OK with usage summary
      {:exit-message (usage summary) :ok? true}
      errors ; errors => exit with description of errors
      {:exit-message (error-msg errors)}
      ;; custom validation on arguments
      (and (= 1 (count arguments))
           (#{"version" "increment" "status"} (first arguments)))
      {:action (first arguments) :args (rest arguments) :options (assoc options :summary summary)}
      :else ; failed custom validation => exit with usage summary
      {:exit-message (usage summary)})))

(defn exit
  ([]
   (exit 0))
  ([code]
   (exit code nil))
  ([code message]
   (throw (ex-info "Exit condition" {:code code :message message}))))

(defn print-version [options]
  (println (:version-str (git/current))))

(defn increment [options args]
  (let [inc-type (some-> args first keyword)]
    (when-not inc-type
      (exit 1 (usage (:summary options))))
    (println inc-type)))

(defn -main [& args]
  (try
    (let [{:keys [action options args exit-message ok?]} (validate-args args)]
      (when exit-message
        (exit (if ok? 0 1) exit-message))
      (case action
        "version"   (print-version options)
        "increment" (increment options args)
        "status"    (println "hmm")))
    (catch Exception e
      (let [{:keys [code message]} (ex-data e)]
        (if message
          (println message)
          (when-not code ; exception
            (log/error e)))
        (System/exit (or code 128)))))
  (System/exit 0))
