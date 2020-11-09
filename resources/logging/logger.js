
var Sentry = require('@sentry/browser')

const ID_PADDING = 25
const CTX_PADDING = 50

const config =
{
	trace0:
	{
		label: 'trace0',
		console: 'debug',
		enabled: false,
		labelColor: 'gray',
	},
	trace:
	{
		label: 'trace',
		console: 'debug',
		breadCrumb: Sentry.Severity.Debug,
		labelColor: 'lightgray',
	},
	debug:
	{
		label: 'debug',
		console: 'log',
		breadCrumb: Sentry.Severity.Debug,
		labelColor: 'gray',
	},
	info:
	{
		label: 'info ',
		console: 'info',
		breadCrumb: Sentry.Severity.Info,
		labelColor: 'blue',
	},
	warn:
	{
		label: 'warn ',
		console: 'warn',
		report: true,
		breadCrumb: Sentry.Severity.Warning,
		labelColor: 'darkorange',
	},
	error:
	{
		label: 'error',
		console: 'error',
		report: true,
		breadCrumb: Sentry.Severity.Critical,
		labelColor: 'red',
	},
}

const getCircularReplacer = () =>
{
	const seen = new WeakSet()

	return (key, value) =>
	{
		if(typeof value === "object" && value !== null)
		{
			if(seen.has(value))
			{
				return
			}

			seen.add(value)
		}

		return value
	}
}

var prettyLogs = false

if(ENV == "client")
{
	console.dir(Object.defineProperty(new Image(), 'id',
	{
		get: () =>
		{
			prettyLogs = true
			return 'console-open-test'
		}
	}))
}

function logger(delegate = console)
{
	Object.entries(config).forEach(([ k, a ]) =>
	{
		this[k] = a.enabled !== false ? console[a.console].bind(console) : function() {}
	})

	this.params = function(level, self, file, context)
	{
		var conf = config[level]

		if(conf.enabled === false)
		{
			return
		}

		var plain = ''
		var params = null
		var args = []
		var prefix, prefixStyles;

		if(prettyLogs)
		{
			prefix = '%c' + conf.label.toUpperCase() + '%c '
			prefixStyles = [ 'background: ' + conf.labelColor + '; color: #fff; font-weight: bold; font-size: 0.9em; padding: 1px 10px', 'background: transparent' ]
		}

		if(DEV && self && self.constructor)
		{
			plain += self.constructor.name+' '

			if(prettyLogs)
			{
				prefix += (('%c' + self.constructor.name + ' ').padEnd(ID_PADDING, " "))
				prefixStyles.push('color: #000')
			}
		}
		else
		{
			plain += file+' '

			if(prettyLogs)
			{
				prefix += (('%c' + file + ' ').padEnd(ID_PADDING, " "))
				prefixStyles.push('color: #000')
			}
		}

		if(context)
		{
			plain += context

			if(prettyLogs)
			{
				prefix += (('%c' + context).padEnd(CTX_PADDING, " "))
				prefixStyles.push('color: #777')
			}
		}

		if(arguments.length > 4)
		{
			params = Array.prototype.slice.call(arguments, 4)
			args = args.concat(params)
		}

		if(conf.report)
		{
			var isObject = function(obj)
			{
				return !!obj && obj instanceof Object && !Array.isArray(obj)
			}

			var errors = []
			var msg = args.map(a =>
			{
				if(a instanceof Error)
				{
					errors.push(a)
					return a.message
				}
				else if(isObject(a))
				{
					return JSON.stringify(a, getCircularReplacer())
				}

				try
				{
					return String(a)
				}
				catch(e)
				{
					return "[error]"
				}
			}).join(" ")

			Sentry.withScope(scope =>
			{
				scope.setExtra('level', level === "error" ? "error" : "warning")
				scope.setExtra('message', msg)
				scope.setExtra('location', file)
				scope.setExtra('errors', errors)

				if(errors.length >= 1)
				{
					// show message in Sentry
					var err = new Error(msg)
					err.name = errors[0].name
					err.stack = errors[0].stack

					Sentry.captureException(err)
				}
				else
				{
					Sentry.captureMessage(msg)
				}
			})
		}

		if(conf.breadCrumb)
		{
			Sentry.addBreadcrumb(
			{
				message: plain + (params ? ": "+params.map(a =>
				{
					try
					{
						return String(a)
					}
					catch(e)
					{
						return "[error]"
					}
				}).join(" ") : ""),
				level: conf.breadCrumb,
				category: 'console',
 			})
		}

		if(prettyLogs)
		{
			return [ prefix ].concat(prefixStyles).concat(args)
		}

		return [ "["+conf.label.toUpperCase()+"] "+plain ].concat(args)
	}
}

export default new logger()
export { logger }
