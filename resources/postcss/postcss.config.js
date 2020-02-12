
const POSTCSS_CONFIG = process.env.POSTCSS_CONFIG

if(!POSTCSS_CONFIG)
{
	throw new Error("Environment variable POSTCSS_CONFIG not defined")
}

module.exports = require(POSTCSS_CONFIG)
