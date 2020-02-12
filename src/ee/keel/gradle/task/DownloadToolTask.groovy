package ee.keel.gradle.task;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.logging.LogLevel
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecSpec

import groovy.transform.CompileStatic;

@CompileStatic
public class DownloadToolTask extends DefaultTask
{
	private final static Logger logger = Logging.getLogger(DownloadToolTask)

	@Input
	final Property<String> toolName = project.objects.property(String)

	@Input
	final Property<Object> location = project.objects.property(Object)

	@Input
	final Property<Boolean> stripFirstDirectory = project.objects.property(Boolean)

	@Internal
	final DirectoryProperty toolsDirectory = project.objects.directoryProperty().convention(project.layout.projectDirectory.dir("tools"))

	@OutputDirectory
	final DirectoryProperty fullOutputDirectory = project.objects.directoryProperty().fileProvider(project.provider({ new File(toolsDirectory.getAsFile().get(), toolName.get()) }))

	public DownloadToolTask()
	{
		logging.captureStandardError(LogLevel.LIFECYCLE)
	}

	@TaskAction
	void run()
	{
		def l = location.get();

		File outDir = fullOutputDirectory.asFile.get()

		if(outDir.exists())
		{
			outDir.deleteDir()
		}

		outDir.mkdirs()

		File downloaded = null;

		try
		{
			if(l instanceof URL)
			{
				URL url = l

				downloaded = toolsDirectory.file(toolName.map { it+".download" }).get().asFile.absoluteFile

				project.exec({ ExecSpec it ->
					it.setExecutable("wget")
					it.setArgs([ l as String, "-O", downloaded.absolutePath ])
				})

				l = downloaded
			}

			if(l instanceof File)
			{
				File file = l

				project.exec({ ExecSpec it ->
					it.setExecutable("tar")
					it.setArgs([ "xf", file.absolutePath, '--directory', fullOutputDirectory.get().asFile.absolutePath ])

					if(stripFirstDirectory.get())
					{
						it.args "--strip", "1"
					}
				})
			}
			else
			{
				throw new IllegalStateException("Unknown input type ${l}")
			}
		}
		finally
		{
			downloaded?.delete()
		}
	}
}
