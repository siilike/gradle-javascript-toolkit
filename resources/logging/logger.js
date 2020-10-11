
var Sentry = require('@sentry/browser');

const getCircularReplacer = () =>
{
	const seen = new WeakSet();

	return (key, value) =>
	{
		if(typeof value === "object" && value !== null)
		{
			if(seen.has(value))
			{
				return;
			}

			seen.add(value);
		}

		return value;
	};
};

function consoleDelegate(level, params, offset)
{
	var fn = console.log;

	if(level == "warn")
	{
		fn = console.warn;
	}
	else if(level == "error")
	{
		fn = console.error;
	}
	else if(level == "info")
	{
		fn = console.info;
	}
	else if(level == "trace")
	{
		fn = console.debug;
	}

	if(offset)
	{
		params = Array.prototype.slice.call(params, offset);
	}

	if(level == "error" || level == "warn")
	{
		var isObject = function(obj)
		{
			return !!obj && obj instanceof Object && !(obj instanceof Array)
		};

		var msgParams = params.slice(1);
		var errors = [];

		var msg = msgParams.map(a =>
		{
			if(a instanceof Error)
			{
				errors.push(a);
				return a.message;
			}
			else if(isObject(a))
			{
				try
				{
					return JSON.stringify(a);
				}
				catch(e)
				{
					return JSON.stringify(a, getCircularReplacer());
				}
			}

			return ""+a;
		}).join(" ");

		if(errors.length >= 1)
		{
			Sentry.withScope(scope =>
			{
				scope.setExtra('level', level == "error" ? "error" : "warning");
				scope.setExtra('message', msg);
				scope.setExtra('location', params[0]);
				scope.setExtra('errors', errors);

				// show message in Sentry
				var err = new Error(msg);
				err.name = errors[0].name;
				err.stack = errors[0].stack;

				Sentry.captureException(err);
			});
		}
		else
		{
			Sentry.withScope(scope =>
			{
				scope.setExtra('level', level == "error" ? "error" : "warning");
				scope.setExtra('message', msg);
				scope.setExtra('location', params[0]);
				scope.setExtra('errors', errors);

				Sentry.captureMessage(msg);
			});
		}
	}

	params.unshift('['+level.toUpperCase()+']');

	fn.apply(console, params);
}

function logger()
{
	this.delegate = global.loggerDelegate || consoleDelegate;

	this.logTransformed = function(level, self, context)
	{
		var args = [];

		if(self && self.constructor)
		{
			var n = self.constructor.name;

			if(!context)
			{
				context = n;
			}
			else if(!context.startsWith(n))
			{
				context = n + "/" + context;
			}

			if(global.utils && utils.uniqueIdEnabled)
			{
				context += "[" + self.__id + "]";
			}
		}

		if(context)
		{
			args.push(context);
		}

		if(arguments.length > 3)
		{
			args = args.concat(Array.prototype.slice.call(arguments, 3));
		}

		this.delegate(level, args);
	};
};

export default new logger();

export { logger, consoleDelegate };
