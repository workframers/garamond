# garamond

A utility to generate and update version numbers and artifact IDs, intended
to be used for assistance in publishing tools.deps-based libraries as jar
files to Maven-based repositories.

The library is meant to be run from a tools.deps alias. It has two main uses:

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

`clojure -A:garamond`

TBD: initialize tags in repo, explain it's all based on git tags

#### leiningen

leiningen users can also set up an alias in `project.clj` to access garamond:

```clojure
(defproject ...
  :aliases {"garamond" ^:pass-through-help ["trampoline" "run" "-m" "garamond.main"]})
```

With this in place, you can run `lein garamond`. Note that garamond does not
have any particular hooks into leiningen internals, but it should be compatible
with plugins such as `lein-v` (see below).

## Usage

`clojure -A:garamond --help` will show the available options:

```
% clojure -A:garamond --help
garamond is a utility for printing and incrementing versions based on git tags.

Usage: clojure -m garamond.main [options] [increment-type]

Options:
  -h, --help                     Print usage and exit
  -v, --verbose                  Print more debugging logs
      --prefix PREFIX            Use this prefix in front of versions for tags
  -p, --pom                      Generate or update the pom.xml file
  -t, --tag                      Create a new git tag based on the given version
  -m, --message MESSAGE          Commit message for git tag
  -g, --group-id GROUP-ID        Update the pom.xml file with this <groupId> value
  -a, --artifact-id ARTIFACT-ID  Update the pom.xml file with this <artifactId> value
      --force-version VERSION    Use this value for the pom.xml <version> tag

With no increment type, garamond will print the current version number and exit.

The prefix string ('v' in the tag 'v1.2.3') will be preserved in the new tag, or
it can be overridden via the -p option.

Increment types:
  major              1.2.4 -> 2.0.0
  minor              1.2.4 -> 1.3.0
  patch              1.2.4 -> 1.2.5
  major-rc           2.7.9 -> 3.0.0-rc.0, 4.0.0-rc.3 -> 4.0.0-rc.4
  minor-rc           2.7.9 -> 2.8.0-rc.0, 4.3.0-rc.0 -> 4.3.0-rc.1
  major-release      4.0.0-rc.4 -> 4.0.0, 3.2.9 -> 4.0.0
  minor-release      8.1.0-rc.4 -> 8.2.0, 5.9.4 -> 5.10.0

See https://github.com/workframers/garamond for more information.
```

* `clojure -A:garamond`: Display the current version based on git tags
* `clojure -A:garamond major`: Increment the major version
* `clojure -A:garamond --pom`: Run `clojure -Spom` and modify the generated pom file
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

The basic goal of this project is to automate all the pre-`pack/pack.alpha`
stuff in [this article about deploying library jars with
deps](https://juxt.pro/blog/posts/pack-maven.html).
Secondarily it aims to serve as an analogue to `lein-v` in the tools.deps
universe.

__TODO__: add more stuff here

### Similar projects

The [lein-v](https://github.com/roomkey/lein-v) and
[lein-git-version](https://github.com/arrdem/lein-git-version) projects
do some similar stuff in a leiningen context.

### About the name

Garamond is the name of a publishing company in Umberto Eco's 1988 novel
_Foucault's Pendulum_, and has nothing to do with the typeface of the same name.

## TODO

- Generate `<scm>` tag, per [this](https://juxt.pro/blog/posts/pack-maven.html#_generate_a_pom_xml).
  Needs lots of argument values, maybe we need a `.garamond.edn` config file?
- Support tag signing
- Autodeploy garamond to clojars when new tags appear on master
- Maybe? support generated version.edn / version.clj file as lein-v
