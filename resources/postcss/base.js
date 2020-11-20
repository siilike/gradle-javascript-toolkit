
module.exports = (ctx, userConf = {}) =>
{
	const v = {}

	var envVars =
	{
		'NODE_ENV': true,
		'BUILD_DIR': true,
		'TOOLS_DIR': true,
		'PROJECT_DIR': true,
		'MODULE': true,
		'MINIFY': true,
		'JSTK_DEBUG': false,
		'HMR': false,
		'BROWSERSLIST': true,
		'POSTCSS_ROOT': true,
		'PREPEND_FILES': false,
		'APPEND_FILES': false,
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
		//
	}, userConf)

	var ret =
	{
		vars: v,
		config:
		{
			map: ctx.options.map,
			parser: ctx.options.parser,
			plugins: [],
		},
		findPlugin: (name, first = true) =>
		{
			var r = ret.config.filter(a => a[0] === name)

			if(r.length === 0)
			{
				return null
			}

			return first ? r[0] : r
		},
		postProcess: config =>
		{

			if(v.JSTK_DEBUG === 'true')
			{
				console.log('>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> POSTCSS CONFIGURATION (pre) >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>')
				console.log(JSON.stringify(config, null, 2))
				console.log('<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< POSTCSS CONFIGURATION (pre) <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<')
			}

			if(Array.isArray(config.plugins))
			{
				config.plugins = config.plugins.map(a =>
				{
					if(typeof a[0] === 'string')
					{
						return require(a[0])(a[1])
					}

					if(a.length === 2)
					{
						return a[0](a[1])
					}

					return a[0]
				})
			}

			if(v.JSTK_DEBUG === 'true')
			{
				console.log('>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> POSTCSS CONFIGURATION >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>')
				console.log(JSON.stringify(config, null, 2))
				console.log('<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< POSTCSS CONFIGURATION <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<')
			}

			return config
		},
	}

	ret.config.plugins.push(...[
		[
			'postcss-import',
			{
				root: v.POSTCSS_ROOT,
			},
		],
		[
			'postcss-use',
			{},
		],
		[
			'postcss-advanced-variables',
			{},
		],
		[
			'postcss-atroot',
			{},
		],
		[
			'postcss-extend-rule',
			{},
		],
		[
			'postcss-nested',
			{},
		],
		[
			'postcss-css-variables',
			{
				preserveAtRulesOrder: true,
			},
		],
		[
			'postcss-preset-env',
			{
				browsers: v.BROWSERSLIST,
			},
		],
		[
			'postcss-property-lookup',
			{},
		],
		[
			'postcss-nested-props',
			{},
		],
		[
			'postcss-push',
			{
				prepend: v.PREPEND_FILES ? JSON.parse(v.PREPEND_FILES) : [],
				append: v.APPEND_FILES ? JSON.parse(v.APPEND_FILES) : [],
			},
		],
	]);

	if(v.MINIFY === 'true' && v.NODE_ENV !== 'development')
	{
		ret.config.plugins.push(
		[
			'cssnano',
			{
				preset:
				[
					'default',
					{
						discardComments:
						{
							removeAll: true,
						},
					},
				],
			},
		])
	}

	return ret
}

