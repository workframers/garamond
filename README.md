# garamond

A utility to generate and update version numbers and artifact IDs, intended
to be used for assistance in publishing tools.deps-based libraries as jar
files to Maven-based repositories.

The library is meant to be run from a tools.deps alias.

The function has two main uses:

1. Maintaining a version number for a library based on git tags
2. Postprocessing the tools.deps `clojure -Spom` output to update it
   with the project's correct artifact ID, group ID, and version number.

## Installation

To use garamond, install it as an alias in your deps.edn:

```clojure
 :aliases
 {...

  :garamond
  {:main-opts ["-m" "garamond.main"]
   :extra-deps {com.workframe/garamond {:mvn/version "0.1.0"}}}

 ...}
```

Now you can run it from the command-line via:

`clojure -A:garamond version`

## Usage

`clojure -A:garamond --help` will show the available options, but the basics are:

* `clojure -A:garamond version`: Display the current version based on git tags
* `clojure -A:garamond bump`: Increment the major/minor/patch version and create a new tag
* `clojure -A:garamond pom`: Run `clojure -Spom` and modify the generated pom file
  to reflect the current version number along with the group-id and artifact-id given.

## Background

### Similar projects

The [lein-v](https://github.com/roomkey/lein-v) and
[lein-git-version](https://github.com/arrdem/lein-git-version) projects
do some similar stuff in a leiningen context.

### About the name

Garamond is the name of a publisher in Umberto Eco's 1988 novel
_Foucault's Pendulum_.
