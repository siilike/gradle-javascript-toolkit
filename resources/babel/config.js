
const TOOLS_DIR = process.env.TOOLS_DIR || './tools'
const BROWSERSLIST = process.env.BROWSERSLIST || '> 0.5%, IE11'
const REACT_PRAGMA = process.env.REACT_PRAGMA || 'React.createElement'
const JSTK_DEBUG = process.env.JSTK_DEBUG || 'false'

module.exports = function(api)
{
 	api.cache(true)

	var config = 
	{
		"presets":
		[
			[
				"@babel/preset-env",
				{
					"useBuiltIns": "usage",
					"corejs": 3,
					"targets": BROWSERSLIST,
				}
			],
			[
				"@babel/preset-react",
				{
					"pragma": REACT_PRAGMA,
				}
			],
		],
		"plugins":
		[
			[ "@babel/plugin-proposal-decorators", { "legacy": true } ],
			[ "@babel/plugin-proposal-class-properties", { "loose": true } ],

			"@babel/plugin-syntax-dynamic-import",
			"@babel/plugin-transform-regenerator",
			"@babel/plugin-proposal-object-rest-spread",

			[ TOOLS_DIR + "/babel/plugin-trace.js",
			{
				"strip":
				{
					"trace0": true,
				},
			}]
		]
	}

	if(JSTK_DEBUG === 'true')
	{
		console.log('>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> BABEL CONFIGURATION >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>')
		console.log(JSON.stringify(config, null, 2))
		console.log('<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< BABEL CONFIGURATION <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<')
	}

	return config
}
