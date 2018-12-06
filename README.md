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

TBD: initialize tags in repo, explain it's all based on git tags

## Usage

`clojure -A:garamond --help` will show the available options, but the basics are:

* `clojure -A:garamond version`: Display the current version based on git tags
* `clojure -A:garamond increment`: Increment the major/minor/patch version and create a new tag
* `clojure -A:garamond pom`: Run `clojure -Spom` and modify the generated pom file
  to reflect the current version number along with the group-id and artifact-id given.

## Versioning rules

garabond uses [`zafarkhaja/jsemver`](https://github.com/zafarkhaja/jsemver)
under the hood to handle manipulating version numbers, and its public
`increment` interface mostly just delegates to methods there.

Accordingly, you can use `clojure -A:garamond increment major` to bump the
major version number, `increment patch` to increment the patchlevel, and
`increment minor` to update the minor version level.

garabond does have some `increment` commands which operate somewhat differently
from the jsemver defaults.

#### `clojure -A:garamond increment major-rc`

Create a "release candidate". If the current version does not have an
`-rc.x` suffix, bump the major version and add a new `-rc.0` suffix.
If it already has a suffix, increment the rc number. So `v1.2.3` would
become `2.0.0-rc.0`, and `3.0.0-rc.2` would become `3.0.0-rc.3`.

#### `clojure -A:garamond increment minor-rc`

This is similar to the above, but if an rc suffix does not exist, the
minor number is incremented instead of the major one, so `v2.4.7`
becomes `v2.5.0` and `v2.8.0-rc.1` becomes  `v2.8.0-rc.2`.

#### `clojure -A:garamond increment major-release`

This performs a release, which will either remove the `-rc.x` suffix
from a version if it has one, or increment the major version if it does
not: `v3.1.2` becomes `v4.0.0` and `v5.0.0-rc.3` becomes `v5.0.0`.

#### `clojure -A:garamond increment minor-release`

This does the same thing as `major-release`, but affects the minor version:
`v2.3.7` becomes `v2.4.0` and `v5.6.0-rc.0` becomes `v5.6.0`.

## Background and rationale

### Similar projects

The [lein-v](https://github.com/roomkey/lein-v) and
[lein-git-version](https://github.com/arrdem/lein-git-version) projects
do some similar stuff in a leiningen context.

### About the name

Garamond is the name of a publishing house in Umberto Eco's 1988 novel
_Foucault's Pendulum_.
