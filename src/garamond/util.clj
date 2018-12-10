(ns garamond.util)

(defn exit
  "Abort garamond and exit with the specified exit code. If a message is passed, print it to stderr
  before exiting. Note that this function just throws exceptions which are handled in garamond.main/-main."
  ([]
   (exit 0))
  ([code]
   (exit code nil))
  ([code message]
   (throw (ex-info "Exit condition" {:code code :message message}))))
