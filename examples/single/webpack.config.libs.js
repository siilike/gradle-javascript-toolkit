
const webpack = require('webpack')

const TOOLS_DIR = process.env.TOOLS_DIR || 'tools'

var { vars, config, postProcess } = require(TOOLS_DIR + '/webpack/base.config.libs.js')

// don't bundle moment's locales
config.plugins.push(new webpack.IgnorePlugin(/^\.\/locale$/, /moment$/))

module.exports = postProcess(config)

