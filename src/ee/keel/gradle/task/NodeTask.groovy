package ee.keel.gradle.task

import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

import ee.keel.gradle.Utils
import ee.keel.gradle.dsl.WithEnvironmentProperties
import groovy.transform.CompileStatic

@CompileStatic
class NodeTask extends ContinuousExecTask implements WithEnvironmentProperties
{
	private final static Logger logger = Logging.getLogger(NodeTask)

	public NodeTask()
	{
		super();

		configure {
			def jstk = Utils.getExt(getProject())

			def n = jstk.node.get()

			setExecutable(n.path.get())

			if(project.hasProperty("debugNode")) {
				args "--inspect"
			}

			args((List<String>) n.args.get())

			environmentProperty "NODE_ENV", jstk.environment

			environment 'BUILD_DIR', project.buildDir.absolutePath
			environment 'PROJECT_DIR', project.projectDir.absolutePath

			if(project.logger.debugEnabled)
			{
				environment "JSTK_DEBUG", "true"
			}
		}
	}
}
