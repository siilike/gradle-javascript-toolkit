package ee.keel.gradle.task

import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input

import ee.keel.gradle.Utils
import groovy.transform.CompileStatic

@CompileStatic
class PnpmTask extends NodeTask
{
	private final static Logger logger = Logging.getLogger(PnpmTask)

	@Input
	final ListProperty<String> command = project.objects.listProperty(String);

	public PnpmTask()
	{
		super();

		configure {
			def ext = Utils.getExt(getProject())
			def pnpmPath = ext.pnpm.get().path.get()

			environment("NODE_PATH", ext.toolsDirectory.get().asFile.absolutePath)

			if(path.endsWith(".js"))
			{
				args(pnpmPath)
			}
			else
			{
				setExecutable(pnpmPath)
				setArgs([])
			}
		}
	}

	@Override
	protected void exec()
	{
		args(command.get())

		super.exec();
	}
}
