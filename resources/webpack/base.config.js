
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

ret.config.optimization.splitChunks =
{
	chunks: 'async',
	minSize: 999999999,
	maxSize: 0,
	minChunks: 1,
	maxAsyncRequests: 4,
	maxInitialRequests: 1,
// 	name: true,
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
		host: '127.0.0.1',
		port: 0,
		https:
		{
			key: fs.readFileSync(TOOLS_DIR + '/certs/cert.key'),
			cert: fs.readFileSync(TOOLS_DIR + '/certs/cert.crt'),
		},
	}));
}

ret.config.plugins.push(
	new JsToolkitPlugin()
)

module.exports = ret
