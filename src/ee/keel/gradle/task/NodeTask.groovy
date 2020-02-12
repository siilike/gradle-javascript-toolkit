package ee.keel.gradle.task

import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.AbstractExecTask
import org.gradle.deployment.internal.DeploymentRegistry
import org.gradle.deployment.internal.DeploymentRegistry.ChangeBehavior
import org.gradle.process.internal.ExecAction

import ee.keel.gradle.Utils
import ee.keel.gradle.task.ContinuousExecTask.ExecDeploymentHandle
import groovy.transform.CompileStatic

@CompileStatic
class NodeTask extends ContinuousExecTask
{
	private final static Logger logger = Logging.getLogger(NodeTask)

	public NodeTask()
	{
		super();

		configure {
			def n = Utils.getExt(getProject()).node.get()

			setExecutable(n.path.get())
			setArgs((List<String>) n.args.get())
		}
	}
}
