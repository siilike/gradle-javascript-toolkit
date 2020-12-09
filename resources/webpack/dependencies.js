
const fs = require('fs')
var zlib = require('zlib')

module.exports = class DependencyTreePlugin
{
	constructor(module, outputDir)
	{
		this.module = module
		this.outputDir = outputDir
	}

	apply(compiler)
	{
		const ignore = new Set([ 'react/jsx-runtime', 'react' ])
		const v = this.vars

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

		const output = l => out.write(l + "\r\n")

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

				var roots = modules.filter(a => (""+a.request).indexOf("/js/"+this.module+"/index.js") != -1)

				if(roots.length !== 1)
				{
					throw new Error("Got "+roots.length+" roots")
				}

				const root = roots[0]

				console.log('Processing root dependency', root.resource)

				let out0;

				try
				{
					out0 = fs.createWriteStream(this.outputDir+"/"+this.module+".deps.gz")

					out = zlib.createGzip()
					out.pipe(out0)

					printDependencies(modules, root, [ root ], new Set([ root ]), new Set())

					console.log('Built dependency tree in ' + (Date.now() - start)+"ms")
				}
				finally
				{
					out?.end?.()
				}
			})
		})
	}
}
