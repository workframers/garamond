(ns garamond.pom
  (:require [taoensso.timbre :as timbre]
            [garamond.version :as version]
            [clojure.tools.deps.alpha.gen.pom :as tool-deps-pom]
            [clojure.java.io :as jio]
            [clojure.data.xml :as xml]
            [clojure.tools.deps.alpha.script.make-classpath :as makecp]
            [clojure.zip :as zip]
            [clojure.data.xml.tree :as tree]
            [clojure.data.xml.event :as event])
  (:import [java.io Reader File]
           [clojure.data.xml.node Element]))

(defn- create-or-sync-pom!
  "Possibly unwisely reaches into some t.d.a internals to kick off pom creation or synchronization.
  Note that this ignores system/user deps.edn files and aliases. Ideally this is the same result as
  running `clojure -Spom`."
  []
  (let [mods (makecp/combine-deps-files {:config-files ["deps.edn"]})]
    (tool-deps-pom/sync-pom mods (jio/file "."))))

;; This block of code stolen wholesale from clojure.tools.deps.alpha.gen.pom, copyright (c) Rich Hickey

(xml/alias-uri 'pom "http://maven.apache.org/POM/4.0.0")

(defn- make-xml-element
  [{:keys [tag attrs] :as node} children]
  (with-meta
   (apply xml/element tag attrs children)
   (meta node)))

(defn- xml-update
  [root tag-path replace-node]
  (let [z (zip/zipper xml/element? :content make-xml-element root)]
    (zip/root
     (loop [[tag & more-tags :as tags] tag-path, parent z, child (zip/down z)]
       (if child
         (if (= tag (:tag (zip/node child)))
           (if (seq more-tags)
             (recur more-tags child (zip/down child))
             (zip/edit child (constantly replace-node)))
           (recur tags parent (zip/right child)))
         (zip/append-child parent replace-node))))))

(defn- parse-xml
  [^Reader rdr]
  (let [roots (tree/seq-tree event/event-element event/event-exit? event/event-node
                             (xml/event-seq rdr {:include-node? #{:element :characters :comment}}))]
    (first (filter #(instance? Element %) (first roots)))))

(defn- replace-artifact-id
  [pom artifact-id]
  (if artifact-id
    (xml-update pom [::pom/artifactId] (xml/sexp-as-element [::pom/artifactId artifact-id]))
    pom))

(defn- replace-group-id
  [pom group-id]
  (if group-id
    (xml-update pom [::pom/groupId] (xml/sexp-as-element [::pom/groupId group-id]))
    pom))

(defn- replace-version
  [pom version-str]
  (if version-str
    (xml-update pom [::pom/version] (xml/sexp-as-element [::pom/version version-str]))
    pom))

(defn update-pom!
  [version {:keys [artifact-id group-id] :as options}]
  (timbre/spy [artifact-id group-id version])
  (let [pom-file (jio/file "pom.xml")
        pom      (with-open [rdr (jio/reader pom-file)]
                   (-> rdr
                       parse-xml
                       (replace-artifact-id artifact-id)
                       (replace-group-id group-id)
                       (replace-version version)))]
    (spit pom-file (xml/indent-str pom))))

;; todo, could add an option to remove pom.xml prior to -Spom
(defn generate!
  "Use tools.deps to create or update a pom.xml, and then post-process it to plug in some values."
  [version options]
  (let [v (.toString version)]
    (timbre/debugf "Generating pom.xml for %s..." v)
    (create-or-sync-pom!)
    (update-pom! v options)))
