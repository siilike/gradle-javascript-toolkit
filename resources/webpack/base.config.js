
const webpack = require('webpack')
const path = require('path')
const fs = require('fs')

const TOOLS_DIR = process.env.TOOLS_DIR || 'tools'

const JsToolkitPlugin = require(TOOLS_DIR + '/webpack/plugin.js')

const ReactRefreshPlugin = require('@pmmmwh/react-refresh-webpack-plugin');
const { WebpackPluginServe } = require('webpack-plugin-serve');

var ret = require(TOOLS_DIR + '/webpack/base.js')()

var nodeModulesRegex = /node_modules/

const v = ret.vars

Object.assign(ret.config,
{
	dependencies: ret.libraries,
	entry:
	{
		main: [ 'index' ],
	},
	output:
	{
		publicPath: '/',
		path: v.OUTPUT_DIR,
		filename: v.MODULE+'-[name]-'+v.ENV+'-'+v.BROWSERSLIST_ENV+'.js',
		sourceMapFilename: v.MODULE+'-[name]-'+v.ENV+'-'+v.BROWSERSLIST_ENV+'.map',
		chunkFilename: v.MODULE+'-[name]-'+v.ENV+'-'+v.BROWSERSLIST_ENV+'.js',
	},
})

ret.config.resolve.modules.splice(1, 0, path.join(v.PROJECT_DIR, 'js/' + v.MODULE))

ret.libraries.forEach(l =>
{
	ret.config.plugins.push(
		new webpack.DllReferencePlugin(
		{
			context: path.resolve('.'),
			manifest: path.join(v.MANIFEST_DIR || v.WEBPACK_DIR, "manifest-"+l+'-'+v.ENV+'-'+v.BROWSERSLIST_ENV+".json"),
		}),
	)
})

/*
ret.config.plugins.push(
	new webpack.optimize.MinChunkSizePlugin(
	{
		minChunkSize: 200000,
	})
)
*/

/*
ret.config.plugins.push(
	new webpack.optimize.LimitChunkCountPlugin(
	{
		maxChunks: 1,
	})
)
*/

ret.config.optimization.splitChunks =
{
	chunks: 'async',
	minSize: 200000,
	maxSize: 999999999,
	minChunks: 1,
	maxAsyncRequests: 6,
	maxInitialRequests: 3,
	cacheGroups:
	{
		vendors: false,
		defaultVendors: false,
		default:
		{
			minChunks: 1,
			priority: -20,
			reuseExistingChunk: true,
		},
	},
}

if(v.NODE_ENV === 'development' && v.HMR === 'true')
{
	ret.config.entry.main.unshift('webpack-plugin-serve/client');

	ret.config.plugins.push(new ReactRefreshPlugin(
	{
		forceEnable: true,
		exclude: nodeModulesRegex,
	}));

	ret.config.plugins.push(new WebpackPluginServe(
	{
		progress: true,
		status: true,
		host: '127.0.0.1',
		port: 0,
		https:
		{
			key: fs.readFileSync(TOOLS_DIR + '/certs/cert.key'),
			cert: fs.readFileSync(TOOLS_DIR + '/certs/cert.crt'),
		},
	}));
}

class DependencyTreePlugin
{
	apply(compiler)
	{
		const ignore = new Set([ 'react/jsx-runtime', 'react' ])

		var out;

		const CLEAN_REGEX = /(.*)\/node_modules\/(.*)/

		const isRelative = (a, relatives, add = true) =>
		{
			var b = a && a.rawRequest && (a.rawRequest.startsWith("./") || a.rawRequest.startsWith("../"))

			if(b && !relatives.has(a))
			{
				if(add)
				{
					relatives.add(a)
				}

				return false
			}

			return b
		}

		const getModuleString = m =>
		{
			if(!m)
			{
				return "INVALID"
			}

			var ret = (m.rawRequest ?? m.userRequest ?? m.request ?? "MISSING")

			// return ret.replace(CLEAN_REGEX, '$2')
			return ret
		}

		const output = l => fs.writeSync(out, l + "\r\n")

		compiler.hooks.compilation.tap('DependencyTreePlugin', compilation =>
		{
			const { moduleGraph } = compilation

			/** @type {Map<Dependency, ModuleGraphDependency>} */
			const dm = moduleGraph._dependencyMap

			const printDependencies = (modules, m, stack, current, relatives) =>
			{
				if(!modules.has(m))
				{
					return
				}

				var v = getModuleString(m)

				if(ignore.has(v))
				{
					return
				}

				var mr = isRelative(m, relatives)

				if(!mr)
				{
					output(stack.map(a => getModuleString(a)).join(" > "))
				}

				if(!m) return;

				var modules = new Set(
					m.dependencies
						.map(a => dm.get(a))
						.filter(a => a.connection && a.connection.getActiveState(undefined))
						.map(a => a.connection.resolvedModule)
				)

				for(const a of modules)
				{
					const mv = getModuleString(a)

					if(current.has(a) || a === m)
					{
						continue
					}

					current.add(a)

					if(stack.indexOf(a) !== -1)
					{
						output(stack.map(a => getModuleString(a)).join(" > ") + " > " + mv + " RECURSION")
						return
					}

					stack.push(a)

					printDependencies(modules, a, stack, isRelative(a, relatives, false) ? current : new Set(), relatives)

					stack.pop()
				}
			}

			compilation.hooks.afterOptimizeChunkModules.tap('DependencyTreePlugin', (chunks, modules) =>
			{
				const start = Date.now()

				var roots = modules.filter(a => (""+a.request).indexOf("/js/"+v.MODULE+"/index.js") != -1)

				if(roots.length !== 1)
				{
					throw new Error("Got "+roots.length+" roots")
				}

				const root = roots[0]

				console.log('Processing root dependency', root.resource)

				try
				{
					out = fs.openSync(v.BUILD_DIR+"/"+v.MODULE+".deps", 'w')

					printDependencies(modules, root, [ root ], new Set([ root ]), new Set())

					console.log('Built dependency tree in ' + (Date.now() - start)+"ms")
				}
				finally
				{
					fs.closeSync(out)
				}
			})
		})
	}
}

if(v.NODE_ENV === 'development' && v.CONTINUOUS !== "true")
{
	ret.config.plugins.push(new DependencyTreePlugin())
}

ret.config.plugins.push(
	new JsToolkitPlugin()
)

module.exports = ret
