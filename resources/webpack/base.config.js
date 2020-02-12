
const webpack = require('webpack')
const path = require('path')

const TOOLS_DIR = process.env.TOOLS_DIR || 'tools'

const JsToolkitPlugin = require(TOOLS_DIR + '/webpack/plugin.js')

var ret = require(TOOLS_DIR + '/webpack/base.js')()

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
			context: '.',
			manifest: path.join(v.MANIFEST_DIR || v.WEBPACK_DIR, "manifest-"+l+'-'+v.ENV+'-'+v.BROWSERSLIST_ENV+".json"),
		}),
	)
})

ret.config.optimization.splitChunks =
{
	chunks: 'async',
	minSize: 0,
	maxSize: 0,
	minChunks: 1,
	maxAsyncRequests: 5,
	maxInitialRequests: 1,
	name: true,
	cacheGroups:
	{
		vendors: false,
		default:
		{
			minChunks: 1,
			priority: -20,
			reuseExistingChunk: true,
		},
	},
}

ret.config.plugins.push(
	new JsToolkitPlugin()
)

module.exports = ret
