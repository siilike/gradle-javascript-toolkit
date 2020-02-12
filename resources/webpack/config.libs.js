
const webpack = require('webpack')

const TOOLS_DIR = process.env.TOOLS_DIR || 'tools'

var { vars, config, postProcess } = require(TOOLS_DIR + '/webpack/base.config.libs.js')

module.exports = postProcess(config)
