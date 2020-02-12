
import * as SentryIntegrations from '@sentry/integrations';

var cache = {};
var own = OWN_FILE_PREFIXES;

export default new SentryIntegrations.RewriteFrames(
{
	iteratee: (frame) =>
	{
		var ret = cache[frame.filename];

		if(!ret)
		{
			var a = frame.filename.split("/").reverse()[0];
			var b = a.split("-")[0];

			if(own.indexOf(b) != -1)
			{
				ret = "/"+a;
			}
			else
			{
				ret = frame.filename;
			}

			cache[frame.filename] = ret;
		}

		frame.filename = ret;

		return frame;
	}
})
