
const zmq = require("zeromq")

const ZMQ_ADDR = process.env.ZMQ_ADDR || false
const ZMQ_ID = process.env.ZMQ_ID || ""+process.pid

const ZMQ_BROADCAST = process.env.ZMQ_BROADCAST || false

module.exports = class JsToolkitPlugin
{
	constructor(serve)
	{
		this.serve = serve
	}

	apply(compiler)
	{
		if(ZMQ_ADDR === false)
		{
			console.log("\nZMQ_ADDR not defined\n")
		}
		else
		{
			const sock = new zmq.Push

			console.log("Connecting to " + ZMQ_ADDR + " as " + ZMQ_ID)

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

		if(ZMQ_BROADCAST === false)
		{
			console.log("\nZMQ_BROADCAST not defined\n")
		}
		else if(!this.serve)
		{
			console.log("\nServe plugin not found\n")
		}
		else
		{
			const sock = new zmq.Subscriber

			sock.connect(ZMQ_BROADCAST)
			sock.subscribe()

			const read = () =>
			{
				sock.receive().then(buf =>
				{
					const msg = JSON.parse(buf.toString())

					if(msg.type === 'done' && /\:build(.*)Css$/.test(msg.content.id))
					{
						console.log("\nSending message done="+msg.content.id+"\n")

						this.serve.emit('css',
						{
							type: msg.type,
							task: msg.content.id,
						})
					}
				}).then(read)
			}

			read()
		}
	}
}
