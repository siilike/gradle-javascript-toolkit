
import car from 'car'; // loads from underlay/shared/car.js
import wheel from 'testlib/wheel'; // loads from libs/testlib/wheel.js

import * as Sentry from '@sentry/browser';

// keep only file names
const frameRewriter = require(TOOLS_DIR + '/sentry/FrameRewriter').default;

Sentry.init(
{
	dsn: 'https://XXX',
	release: SENTRY_RELEASE,
	integrations:
	[
		frameRewriter,
	]
});

// run application
