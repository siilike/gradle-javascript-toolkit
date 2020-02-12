
const zmq = require("zeromq")

const ZMQ_ADDR = process.env.ZMQ_ADDR || false
const ZMQ_ID = process.env.ZMQ_ID || ""+process.pid

module.exports = class JsToolkitPlugin
{
	constructor(options)
	{
		this.options = options
	}

	apply(compiler)
	{
		if(ZMQ_ADDR === false)
		{
			console.log("\nZMQ_ADDR not defined\n")
			return;
		}

		const sock = new zmq.Push

		console.log("Connecting to "+ZMQ_ADDR+" as "+ZMQ_ID)

		sock.connect(ZMQ_ADDR)
		sock.sendTimeout = 0

		sock.send(JSON.stringify({ id: ZMQ_ID, event: 'init' }))

		compiler.hooks.watchRun.tap('JsToolkitPlugin', () =>
		{
			sock.send(JSON.stringify({ id: ZMQ_ID, event: 'watchRun' }))
		})

		compiler.hooks.beforeRun.tap('JsToolkitPlugin', () =>
		{
			sock.send(JSON.stringify({ id: ZMQ_ID, event: 'beforeRun' }))
		})

		compiler.hooks.done.tap('JsToolkitPlugin', () =>
		{
			sock.send(JSON.stringify({ id: ZMQ_ID, event: 'done' }))
		})

		compiler.hooks.failed.tap('JsToolkitPlugin', () =>
		{
			sock.send(JSON.stringify({ id: ZMQ_ID, event: 'failed' }))
		})
	}
}
