
# Gradle JavaScript Toolkit

This is a plugin for JavaScript development using Gradle as the build tool based on:

  * NodeJS
  * pnpm
  * Webpack (5+)
  * PostCSS (8+)
  * Babel

It also supports continuous building, splitting vendor and application code, Sentry, runtime presets, packaging and downloading required external software (node, pnpm).

There are three plugins:

  * JsToolkitBasePlugin -- only the DSL
  * JsToolkitToolsPlugin -- all tasks for installing build tools and dependencies
  * JsToolkitPlugin -- everything, creates all relevant tasks

The main outputs are modules and libraries. Note that modules are not subprojects -- you should use actual Gradle subprojects, because they can be built in parallel. Subprojects use their parents' build tools by default.

## Usage

Usage in `build.gradle`:

```
plugins {
	id "ee.keel.gradle.JsToolkitPlugin" version "1.1"
}

jsToolkit {
	// for a quick start see https://github.com/siilike/gradle-javascript-toolkit/blob/master/examples/single/build.gradle
}
```

## Dependencies

  * Gradle that is supported by the gradle-ospackage-plugin, tested with Gradle 6.6
  * wget and tar for downloading and unpacking node and pnpm
  * sh for uploading assets to Sentry

## Configuration

Default configuration files for JavaScript tools:

  * Babel: `resources/babel/config.js`
  * PostCSS: `resources/postcss/config.js`
  * WebPack: `resources/webpack/base.js` (general), `resources/webpack/base.config.js` (app), `resources/webpack/base.config.libs.js` (libraries)

You are free to extend or override those, but the configuration needs to be compatible with the plugin.

Configuration files are resolved as follows:

  * {tool}.config.{module}.js
  * {tool}.config.js
  * plugin default

It is also possible to define the location of the configuration files in the plugin DSL.

## Directory structure

  * `css/{module}` -- CSS files for `{module}`, joined in their natural order
  * `js/{module}` -- JS files for `{module}` with `index.js` as the entry point
  * `libs` -- contains any local libraries that can be imported just like `npm` ones
  * `templates` -- templates for generating runtimes -- the component that displays a loading message and loads all scripts
  * `tools` -- node, pnpm, build tools -- should be added to `.gitignore`
  * `underlay` -- resolved after no match found from `js/{module}`

## JavaScript file resolving order

  * `./node_modules`
  * `js/{module}` (module builds only)
  * `libs-{env}`
  * `libs`
  * `underlay-{env}`
  * `underlay`
  * `underlay-{env}/*`
  * `underlay/*`
  * `build/node_modules`
  * `tools/node_modules`

The environment-dependent variants are mainly to ease working with git submodules: you can add the submodule to e.g. `libs`, but locally create a symlinked version in `libs-development`.

# DSL

See `src/ee/keel/gradle/dsl/` for all possible options.

## Node and pnpm versions

```
node {
	version = "local" // default
	version = file("node.tar.gz")
	version = new URL("http://nodejs.org/dist/v12.14.1/node-v12.14.1-linux-x64.tar.gz")
}
```

## Build tools

```
packages {
	version "@babel/core", "latest"
	version "webpack", "latest"
}
```

See defaults in `src/ee/keel/gradle/dsl/JsToolkitExtension.groovy`.

After a successful build you should copy `tools/pnpm-lock.yaml` as `pnpm-lock-tools.yaml` into the root directory for predictable builds and also add it to version control.

## Babel

```
babel {
	preset "production", "ie 11, Firefox ESR"
	preset "modern", "last 1 chrome version, last 1 firefox version"
	preset "ssr", "node 12"
}
```

Defines the different presets to build.

## Outputs

```
outputs {
	client {
		preset "production", "modern"
	}
	server {
		preset "ssr"
		minify = false
	}
}
```

Defines outputs and their presets.

## Libraries

```
libraries {
	libs {
		include "react"
	}
}
```

Defines a library "libs" that includes the "react" library. When no includes are defined defaults to all "dependencies" in `package.json`.

## Modules

```
modules {
	example {
		library "libs"

		css "20-test.css", "test.css"
	}
}
```

Defines a module "example" that depends on the library "libs". The file "test.css" will be added to the CSS stack as "20-test.css".

Modules have a single input at `js/{module}/index.js`. You can create additional chunks with `import()`:

```
import(/* webpackChunkName: "admin" */ "pages/admin/Index")
```

which would emit chunks `example-main-{output}-{preset}.js` and `example-admin-{output}-{preset}.js`. The latter one would contain `pages/admin/Index` and its dependencies.

## Distribution

```
distributions {
	libraries {
		library = true

		// copy from the relevant libraries directory
		copyLibrary("libs") { Directory a ->
			from {
				fileTree(a) {
					// include all "client" output presets
					include "client/**"
					// include version file
					include ".version"
				}.files
			}
		}

		// packaging
		repo {
			// final name will be example-static-libs-{version}
			name = "example-static-libs"
			// where the files are located after installing the package
			path = "/data1/apps/example/static/l${librariesVersion.get()}/"
		}
	}

	example {
		// register dependency on "libraries", so it will get installed when the module is installed
		dependsOn "libraries"

		// copy form the relevant module directory
		copyModule("example") { Directory a ->
			from {
				fileTree(a) {
					include "client/**"
					include "css/**"
					include ".version"
					include ".version-*"
				}.files
			}
		}

		// packaging
		repo {
			// final name will be example-static-{version}
			name = "example-static"
			path = "/data1/apps/example/static/v${version.get()}/"
		}
	}
}
```

## Runtimes

```
runtimes {
	example {
		// define module(s), so the relevant files are passed as template variables
		module "example"
		template = file("templates/example.tpl")
		templateVar "test", 5
		indexFileName = "index.php"

		distribution {
			// package name not version-dependent by default, but could be
			repo {
				path = "/data1/apps/example/runtime/"
			}
		}
	}
}
```

Runtimes display a loading message and load the relevant JavaScript files. See the examples.

## Sentry

```
sentry {
	token = "XXX"
	url = "https://sentry.XXX/"
	organization = "main"
	projectName = "example"
}
```

Files are uploaded to Sentry without paths. This means that on the client the path information also needs to be removed:

```
const frameRewriter = require(TOOLS_DIR + '/sentry/FrameRewriter').default;

Sentry.init(
{
	dsn: '...',
	release: SENTRY_RELEASE,
	integrations:
	[
		frameRewriter,
	]
});
```

## Logging

The plugin includes a processor for label-based logging. This means that you can simply write, for example:

```
warn: 'User', id, 'not found';
```

which would get transformed into

```
logger.logTransformed(level, self, context, ...args)
```

which by default would log all available context information along with the message:

```
[WARN] RuntimeClassName/FileName:ClassName:MethodName[anonymous@18] User 5 not found
```

and additionally send it to Sentry for the "warn" and "error" levels. Any error objects supplied as arguments are included for stack trace.

Available labels are:

  * `trace`
  * `debug`
  * `info`
  * `warn`
  * `error`

A global `logger` object must be initialized first:

```
global.logger = require(TOOLS_DIR + "/logging/logger").default;
```

# Tasks and options

See `gradle tasks --all` for all available tasks.

The most important tasks are:

  * `buildFrontend` -- builds all modules and libraries
  * `buildRuntimes` -- builds all runtimes
  * `distAll` -- builds distributions of all modules and libraries
  * `distAllDeb` -- builds deb packages of all modules and libraries
  * `distRuntimes` -- builds all distributions of runtimes
  * `distRuntimesDeb` -- builds deb packages of all runtimes

Available project options:

  * version -- the version of the library, module or runtime to be built
  * dev -- enables development mode, disables minification

For example:

```
gradle -Pdev buildFrontend
gradle -Pversion=5.master distAllDeb
```

## Handling versions

Generally it is useful to use build numbers as versions. For multibranch projects the branch name can also be included.

Library versions are automatically tracked, they change when the library gets rebuilt.

## Continuous build

The plugin supports running all Node tasks continuously. By default only module tasks are run continuously, any modifications that would cause libraries to be rebuilt or other major changes require restarting the build.

Gradle waits for the relevant build to finish before continuing with other tasks, so the task outputs can be used as inputs for other tasks.
