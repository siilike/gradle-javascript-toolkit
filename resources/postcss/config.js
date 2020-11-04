
const TOOLS_DIR = process.env.TOOLS_DIR || 'tools'

module.exports = ctx =>
{
	var { vars, config, postProcess } = require(TOOLS_DIR + '/postcss/base.config.js')(ctx)

	return postProcess(config)
}

