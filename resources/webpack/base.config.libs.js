
const webpack = require('webpack')
const path = require('path')

const TOOLS_DIR = process.env.TOOLS_DIR || 'tools'

var ret = require(TOOLS_DIR + '/webpack/base.js')()

const v = ret.vars

const entries = process.env.ENTRIES ? process.env.ENTRIES.split(",") : [];

Object.assign(ret.config,
{
	entry: entries,
	output:
	{
		publicPath: '/',
		path: v.OUTPUT_DIR,
		filename: v.MODULE+'-'+v.ENV+'-'+v.BROWSERSLIST_ENV+'.js',
		sourceMapFilename: v.MODULE+'-'+v.ENV+'-'+v.BROWSERSLIST_ENV+'.map',
		chunkFilename: v.MODULE+'-'+v.ENV+'-'+v.BROWSERSLIST_ENV+'.js',
		library: v.MODULE,
	},
})

ret.config.plugins.push(
	new webpack.DllPlugin(
	{
		name: v.MODULE,
		path: path.join(v.MANIFEST_DIR || v.WEBPACK_DIR, "manifest-"+(v.LIBS_ID || v.MODULE+'-'+v.ENV+'-'+v.BROWSERSLIST_ENV)+".json")
	}),
)

module.exports = ret
