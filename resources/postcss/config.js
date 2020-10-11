
const JSTK_DEBUG = process.env.JSTK_DEBUG || 'false'
const BROWSERSLIST = process.env.BROWSERSLIST || '> 0.5%, IE11'
const POSTCSS_ROOT = process.env.POSTCSS_ROOT || '.'

module.exports = ctx =>
{
	var config = 
	{
		map: ctx.options.map,
		parser: ctx.options.parser,
		plugins:
		{
			'postcss-import':
			{
				root: POSTCSS_ROOT,
			},
			'postcss-use': {},
			'postcss-advanced-variables': {},
			'postcss-atroot': {},
			'postcss-extend-rule': {},
			'postcss-nested': {},
			'postcss-css-variables':
			{
				preserveAtRulesOrder: true,
			},
			'postcss-preset-env':
			{
				browsers: BROWSERSLIST,
			},
			'postcss-property-lookup': {},
			'postcss-nested-props': {},
// 			'postcss-url': {},
			'cssnano': ctx.env === 'production' ? {} : false,
		}
	}

	if(JSTK_DEBUG === 'true')
	{
		console.log('>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> POSTCSS CONFIGURATION >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>')
		console.log(JSON.stringify(config, null, 2))
		console.log('<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< POSTCSS CONFIGURATION <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<')
	}

	return config
}
