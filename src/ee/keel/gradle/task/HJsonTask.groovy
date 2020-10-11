package ee.keel.gradle.task

import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemLocationProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.logging.LogLevel
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile

import ee.keel.gradle.Utils
import ee.keel.gradle.dsl.WithEnvironmentProperties
import groovy.transform.CompileStatic

@CompileStatic
class HJsonTask extends NodeTask
{
	private final static Logger logger = Logging.getLogger(HJsonTask)

	@InputFile
	final RegularFileProperty input = project.objects.fileProperty()

	@OutputFile
	final RegularFileProperty output = project.objects.fileProperty()

	public HJsonTask()
	{
		super();
	}

	@Override
	protected void exec()
	{
		def ext = Utils.getExt(project)

		args ((List<String>) [
			ext.toolsDirectory.get().file("node_modules/hjson/bin/hjson").asFile.absolutePath,
			'-json',
			input.asFile.get().absolutePath,
		])

		standardOutput = new FileOutputStream(output.asFile.get())

		super.exec()
	}
}
