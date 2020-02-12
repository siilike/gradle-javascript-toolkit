package ee.keel.gradle.task;

import javax.inject.Inject

import org.gradle.api.NonExtensible
import org.gradle.api.logging.LogLevel
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.AbstractExecTask
import org.gradle.api.tasks.Exec;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal
import org.gradle.deployment.internal.Deployment
import org.gradle.deployment.internal.DeploymentHandle
import org.gradle.deployment.internal.DeploymentRegistry
import org.gradle.deployment.internal.DeploymentRegistry.ChangeBehavior
import org.gradle.process.internal.ExecAction

import ee.keel.gradle.StreamLogger
import ee.keel.gradle.ZMQHandler
import groovy.transform.CompileStatic;

@CompileStatic
public class ContinuousExecTask extends Exec
{
	private final static Logger logger = Logging.getLogger(ContinuousExecTask)

	@Input
	final Property<Boolean> continuous = project.objects.property(Boolean).convention(false)

	public ContinuousExecTask()
	{
		logging.captureStandardOutput(getStdoutLogLevel())
		logging.captureStandardError(getStdErrLogLevel())
	}

	@Internal
	public LogLevel getStdoutLogLevel()
	{
		return LogLevel.INFO
	}

	@Internal
	public LogLevel getStdErrLogLevel()
	{
		return LogLevel.WARN
	}

	protected boolean continuousRunning()
	{
		return true
	}

	@Override
	protected void exec()
	{
		if(continuous.get())
		{
			logger.debug("Running {} continuously", getPath())

			def deploymentRegistry = services.get(DeploymentRegistry)
			def deploymentHandle = deploymentRegistry.get(getPath(), ExecDeploymentHandle)
			def zmq, l;

			if(!deploymentHandle)
			{
				def field = AbstractExecTask.getDeclaredField("execAction")
				field.setAccessible(true)
				def action = (ExecAction) field.get(this)

				zmq = new ZMQHandler()
				zmq.init()

				l = zmq.getStatusSync(getPath())

				environment "ZMQ_ADDR", "tcp://127.0.0.1:"+zmq.port
				environment "ZMQ_ID", getPath()

				if(continuousRunning() && !l.isRunning())
				{
					l.running()
				}

				action.setStandardOutput(new StreamLogger(logger, getStdoutLogLevel()))
				action.setErrorOutput(new StreamLogger(logger, getStdErrLogLevel()))

				deploymentRegistry.start(getPath(), ChangeBehavior.BLOCK, ExecDeploymentHandle, action, zmq)
			}
			else
			{
				zmq = deploymentHandle.zmqHandler
				l = zmq.getStatusSync(getPath())
			}

			logger.lifecycle("Awaiting {} to finish", getPath())

			l.await()

			logger.debug("{} finished", getPath())
		}
		else
		{
			super.exec()
		}
	}

	@CompileStatic
	@NonExtensible
	public static class ExecDeploymentHandle implements DeploymentHandle
	{
		private final static Logger logger = Logging.getLogger(ExecDeploymentHandle)

		protected final ExecAction execAction
		protected Thread thread
		protected ZMQHandler zmqHandler

		@Inject
		public ExecDeploymentHandle(ExecAction execAction, ZMQHandler zmqHandler)
		{
			this.execAction = execAction
			this.zmqHandler = zmqHandler
		}

		public ZMQHandler getZmqHandler()
		{
			return zmqHandler
		}

		@Override
		public boolean isRunning()
		{
			return thread && thread.isAlive()
		}

		@Override
		public void start(Deployment deployment)
		{
			logger.lifecycle("Starting {}", execAction.executable)

			thread = new Thread() {
				public void run()
				{
					execAction.execute()
				}
			};

			thread.start()
		}

		@Override
		public void stop()
		{
			logger.lifecycle("Stopping {}", execAction.executable)

			if(thread)
			{
				thread.interrupt()
			}
		}
	}
}
