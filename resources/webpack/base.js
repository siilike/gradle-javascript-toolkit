
const webpack = require('webpack')
const path = require('path')
const fs = require('fs')

const Visualizer = require('webpack-visualizer-plugin')
const TerserJsPlugin = require('terser-webpack-plugin')
const WebpackNotifierPlugin = require('webpack-notifier');

const FULLY_SPECIFIED = false

module.exports = userConf =>
{
	const v = {}

	var envVars =
	{
		'NODE_ENV': true,
		'VERSION': true,
		'SENTRY_RELEASE': true,
		'LIBS_ID': false,
		'BUILD_DIR': true,
		'MANIFEST_DIR': false,
		'RECORDS_DIR': false,
		'TOOLS_DIR': true,
		'PROJECT_DIR': true,
		'BABEL_CACHE_DIR': true,
		'BABEL_CONFIG': true,
		'OUTPUT_DIR': false,
		'ENV': true,
		'BROWSERSLIST_ENV': true,
		'MODULE': true,
		'MINIFY': true,
		'PREFER_MODULES': true,
		'JSTK_DEBUG': false,
		'ALWAYS_TRANSPILE': false,
		'HMR': false,
		'RESOLVE_FILE': false,
		'SOURCE_MAP': false,
		'CONTINUOUS': false,
	}

	Object.entries(envVars).forEach(([ a, required ]) =>
	{
		if(!process.env[a])
		{
			if(required)
			{
				throw new Error("Environment variable "+a+" is required")
			}

			return;
		}

		v[a] = process.env[a]
	})

	const config = Object.assign(
	{
		preferModules: v.PREFER_MODULES === 'true'
	}, userConf)

	if(v.JSTK_DEBUG === 'true')
	{
		console.log('>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> WEBPACK VARS >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>')
		console.log(JSON.stringify(v, null, 2))
		console.log('<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< WEBPACK VARS <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<')

		console.log('>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> WEBPACK CONFIGURATION >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>')
		console.log(JSON.stringify(config, null, 2))
		console.log('<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< WEBPACK CONFIGURATION <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<')
	}

	const libraries = process.env.LIBRARIES ? process.env.LIBRARIES.split(",") : [];

	var ret =
	{
		vars: v,
		config: {},
		package: require(v.BUILD_DIR + '/package.json'),
		postProcess: config =>
		{
			if(v.JSTK_DEBUG === 'true')
			{
				console.log('>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> WEBPACK CONFIGURATION >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>')
				console.log(JSON.stringify(config, null, 2))
				console.log('<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< WEBPACK CONFIGURATION <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<')
			}

			if(v.RESOLVE_FILE)
			{
				fs.writeFileSync(v.RESOLVE_FILE, "module.exports = { resolve: \n\n" + JSON.stringify(config.resolve, null, 2) + "\n\n}")
			}

			return config
		},
		libraries: libraries,
	}

	var underlayDir = path.join(v.PROJECT_DIR, 'underlay')
	var underlayDirWithEnv = path.join(v.PROJECT_DIR, 'underlay-'+v.NODE_ENV)

	var listUnderlayDir = (underlayDir) => fs.readdirSync(underlayDir)
		.filter(a => a[0] != '.')
		.map(a => fs.realpathSync(path.join(underlayDir, a)))
		.filter(a => fs.lstatSync(a).isDirectory())

	var mainFields = config.preferModules ? [ 'module', 'jsnext:main', 'browser', 'main' ] : [ 'browser', 'module', 'main' ]

	var babelOptions =
	{
		cacheDirectory: v.BABEL_CACHE_DIR,
		configFile: v.BABEL_CONFIG,
		cwd: v.TOOLS_DIR,
	}

	var envDepsOptions =
	{
		mainFields: mainFields,
		path: v.BUILD_DIR + '/node_modules/',
		engine:
		{
			node: process.version.substring(1),
		},
	};

	var nodeModulesRegex = /node_modules/
	var babelIncludes = v.ALWAYS_TRANSPILE === 'true' ? null : require('webpack-babel-env-deps').include(envDepsOptions)

	if(v.JSTK_DEBUG === 'true')
	{
		console.log('>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> WEBPACK CONFIGURATION >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>')
		console.log("babelIncludes = " + babelIncludes)
		console.log("babelOptions = " + JSON.stringify(babelOptions, null, 2))
		console.log("envDepsOptions = " + JSON.stringify(envDepsOptions, null, 2))
		console.log('<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< WEBPACK CONFIGURATION <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<')
	}

	Object.assign(ret.config,
	{
		mode: v.NODE_ENV,
		name: v.MODULE,
		devtool: v.HMR === 'true' ? false : (v.SOURCE_MAP || false),
		optimization:
		{
			minimize: false,
			usedExports: v.NODE_ENV == 'production',
		},
		module:
		{
			rules:
			[
				{
					test: /\.(mjs|cjs|js|jsx)$/,
					resolve:
					{
						fullySpecified: FULLY_SPECIFIED,
					},
					exclude: v.ALWAYS_TRANSPILE === 'true' ? (a) => false :
					[
						(a) => nodeModulesRegex.test(a) && !babelIncludes.test(a),
					],
					use:
					[
						{
							loader: 'babel-loader',
							options: babelOptions,
						},
					]
				},
			],
		},
		resolve:
		{
			mainFields: mainFields,
			extensions: [ '.mjs', '.cjs', '.jsx', '.js' ],
			fullySpecified: FULLY_SPECIFIED,
			modules:
			[
				'./node_modules',

				path.join(v.PROJECT_DIR, 'libs-'+v.NODE_ENV),
				path.join(v.PROJECT_DIR, 'libs'),
				underlayDirWithEnv,
				underlayDir,
			].concat(
				fs.existsSync(underlayDirWithEnv) ? listUnderlayDir(underlayDirWithEnv) : []
			).concat(
				fs.existsSync(underlayDir) ? listUnderlayDir(underlayDir) : []
			).concat(
			[
				path.join(v.BUILD_DIR, 'node_modules'),
				path.join(v.TOOLS_DIR, 'node_modules'),
			]),
			fallback: {},
			alias: {},
		},
		resolveLoader:
		{
			mainFields: mainFields,
			fullySpecified: FULLY_SPECIFIED,
			modules:
			[
				'./node_modules',
				path.join(v.PROJECT_DIR, 'libs-'+v.NODE_ENV),
				path.join(v.PROJECT_DIR, 'libs'),
				path.join(v.BUILD_DIR, 'node_modules'),
				path.join(v.TOOLS_DIR, 'node_modules'),
			],
		},
		recordsPath: path.join(v.RECORDS_DIR || v.WEBPACK_DIR, 'records-'+v.MODULE+'-'+v.ENV+'-'+v.BROWSERSLIST_ENV),
		plugins:
		[
			new webpack.DefinePlugin(
			{
				'DEV': JSON.stringify(v.NODE_ENV == 'development'),
				'process.env': { NODE_ENV: JSON.stringify(v.NODE_ENV) },

				'VERSION': JSON.stringify(v.VERSION),
				'SENTRY_RELEASE': JSON.stringify(v.SENTRY_RELEASE),
				'OWN_FILE_PREFIXES': JSON.stringify([ v.MODULE ].concat(libraries)),
				'TOOLS_DIR': JSON.stringify(v.TOOLS_DIR),

				'MODULE': JSON.stringify(v.MODULE),
				'ENV': JSON.stringify(v.ENV),

				'CONTINUOUS': JSON.stringify(v.CONTINUOUS),
			}),
		],
		output:
		{
			chunkLoadTimeout: 20000,
			crossOriginLoading: 'anonymous',
			hotUpdateChunkFilename: v.MODULE+'.hmr-[id]-[hash].js',
		}
	})

	if(v.JSTK_DEBUG === "true" && v.CONTINUOUS !== "true")
	{
		// doesn't work with hmr or continuous mode
		ret.config.plugins.push(
			new webpack.debug.ProfilingPlugin(
			{
				outputPath: v.BUILD_DIR + '/' + v.MODULE + '.profile.json',
			})
		)
	}

	if(v.CONTINUOUS !== "true")
	{
		ret.config.plugins.push(new Visualizer(
		{
			filename: "../webpack-" + v.MODULE + '-' + v.ENV + '-' + v.BROWSERSLIST_ENV + ".html",
		}))
	}
	else
	{
		ret.config.optimization.removeAvailableModules = false
		ret.config.optimization.removeEmptyChunks = false
		ret.config.output.pathinfo = false

		ret.config.watchOptions =
		{
			ignored:
			[
				'node_modules/**',
			],
		}

		ret.config.plugins.push(
			new WebpackNotifierPlugin(
			{
				title: v.MODULE,
				excludeWarnings: true,
				onlyOnError: true,
			})
		)
	}

	if(v.MINIFY === 'true' && v.NODE_ENV !== 'development')
	{
		ret.config.optimization.minimize = true
		ret.config.optimization.minimizer =
		[
			new TerserJsPlugin(
			{
				parallel: true,
				terserOptions:
				{
					compress:
					{
					},
					output:
					{
						comments: false,
					},
				},
			}),
		]
	}

	return ret
}
