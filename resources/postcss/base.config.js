
const TOOLS_DIR = process.env.TOOLS_DIR || 'tools'

module.exports = ctx =>
{
	var ret = require(TOOLS_DIR + '/postcss/base.js')(ctx)

	return ret
}

