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
class PostCssTask extends NodeTask implements WithEnvironmentProperties
{
	private final static Logger logger = Logging.getLogger(PostCssTask)

	@Input
	final ListProperty<File> input = project.objects.listProperty(File)

	@OutputFile
	final RegularFileProperty output = project.objects.fileProperty()

	@InputDirectory
	final DirectoryProperty rootDirectory = project.objects.directoryProperty().convention(project.layout.projectDirectory.dir("css"))

	@InputFile
	final RegularFileProperty config = project.objects.fileProperty()

	@Input
	final Property<String> browsers = project.objects.property(String)

	public PostCssTask()
	{
		super();

		configure {
			def ext = Utils.getExt(project)

			inputs.property "environment", ext.environment

			environmentDirProvider "NODE_PATH", ext.toolsDirectory.dir("node_modules")
			environmentProperty "POSTCSS_ROOT", rootDirectory
			environmentProperty "POSTCSS_CONFIG", config
			environmentProperty "BROWSERSLIST", browsers
		}
	}

	@Override
	protected void exec()
	{
		def ext = Utils.getExt(project)

		applyEnvironmentProperties()

		args ((List<String>) [
			ext.toolsDirectory.get().file("node_modules/postcss-cli/bin/postcss").asFile.absolutePath,
			'--no-map', // inline
			'--map',
			'--verbose',
			'--env', ext.environment.get(),
			'--parser', 'postcss-scss',
			'--config', ext.toolsDirectory.get().dir("postcss").asFile.absolutePath,
			'-o', output.asFile.get().absolutePath,
		])

		((List<File>) input.get()).each { File f ->
			args f.absolutePath
		}

		if(continuous.get())
		{
			args '--watch'
		}

		super.exec()
	}
}
