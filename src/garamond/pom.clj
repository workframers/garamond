(ns garamond.pom
  (:require [taoensso.timbre :as timbre]
            [clojure.tools.deps.alpha.gen.pom :as tool-deps-pom]
            [clojure.java.io :as jio]
            [clojure.data.xml :as xml]
            [clojure.tools.deps.alpha.script.make-classpath :as makecp]
            [clojure.zip :as zip]))

;; Some decent xml zipper background:
;; https://ravi.pckl.me/short/functional-xml-editing-using-zippers-in-clojure/
;; http://josf.info/blog/2014/03/28/clojure-zippers-structure-editing-with-your-mind/

(defn- create-or-sync-pom!
  "Possibly unwisely reaches into some t.d.a internals to kick off pom creation or synchronization.
  Note that this ignores system/user deps.edn files and aliases. Ideally this is the same result as
  running `clojure -Spom`."
  []
  (let [mods (makecp/combine-deps-files {:config-files ["deps.edn"]})]
    (tool-deps-pom/sync-pom mods (jio/file "."))))

;; This block of code stolen wholesale from clojure.tools.deps.alpha.gen.pom, copyright (c) Rich Hickey

(xml/alias-uri 'pom "http://maven.apache.org/POM/4.0.0")

(defn simple-tag [node new-val]
  (assoc-in node [:content] new-val))

(defn tag-matches? [loc [parent-tag child-tag]]
  (and (= (-> loc zip/node :tag) child-tag)
       (= (-> loc zip/up zip/node :tag) parent-tag)))

;; NB: this probably needlessly recurses through the whole tree, but only changes the top level
(defn replace-tag
  "Given an xml-zip result, the path to a tag, and the new contents of a node, return the xml with the
  contents of the tag replaced by the given result."
  [pom-zip tag-path contents]
  (loop [loc pom-zip]
    (if (zip/end? loc)
      (zip/xml-zip (zip/root loc))
      (if (tag-matches? loc tag-path)
        (recur (zip/next (zip/edit loc simple-tag contents)))
        (recur (zip/next loc))))))

(defn- replace-artifact-id
  [pom-zip artifact-id]
  (if artifact-id
    (replace-tag pom-zip [::pom/project ::pom/artifactId] artifact-id)
    pom-zip))

(defn- replace-group-id
  [pom-zip group-id]
  (if group-id
    (replace-tag pom-zip [::pom/project ::pom/groupId] group-id)
    pom-zip))

(defn- replace-version
  [pom-zip version-str]
  (if version-str
    (replace-tag pom-zip [::pom/project ::pom/version] version-str)
    pom-zip))

(defn update-pom!
  [version {:keys [artifact-id group-id] :as options}]
  (timbre/spy [artifact-id group-id version])
  (let [pom-file (jio/file "pom.xml")
        pom      (with-open [rdr (jio/reader pom-file)]
                   (-> rdr
                       xml/parse
                       zip/xml-zip
                       (replace-artifact-id artifact-id)
                       (replace-group-id group-id)
                       (replace-version version)
                       zip/root))
        content (xml/indent-str pom)]
    (spit pom-file content)))

;; todo, could add an option to remove pom.xml prior to -Spom
(defn generate!
  "Use tools.deps to create or update a pom.xml, and then post-process it to plug in some values."
  [version options]
  (let [v (.toString version)]
    (timbre/debugf "Generating pom.xml for %s..." v)
    (create-or-sync-pom!)
    (update-pom! v options)))
