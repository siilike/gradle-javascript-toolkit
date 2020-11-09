package ee.keel.gradle.task

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile

import ee.keel.gradle.Utils
import groovy.json.JsonOutput
import groovy.transform.CompileStatic

@CompileStatic
class PostCssTask extends NodeTask
{
	private final static Logger logger = Logging.getLogger(PostCssTask)

	@Input
	final Property<String> module = project.objects.property(String)

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

	@Input
	final Property<Boolean> minify = project.objects.property(Boolean).convention(true)

	@Input
	final ListProperty<File> prepend = project.objects.listProperty(File).convention([])

	@Input
	final ListProperty<File> append = project.objects.listProperty(File).convention([])

	public PostCssTask()
	{
		super();

		configure {
			def jstk = Utils.getExt(project)

			environmentDirProvider "TOOLS_DIR", jstk.toolsDirectory

			environmentProperty "MODULE", module
			environmentProperty "POSTCSS_ROOT", rootDirectory
			environmentProperty "POSTCSS_CONFIG", config
			environmentProperty "BROWSERSLIST", browsers
			environmentProperty "MINIFY", minify
			environmentProvider "PREPEND_FILES", prepend.map({ a -> JsonOutput.toJson(a.collect { File f -> f.absolutePath }) })
			environmentProvider "APPEND_FILES", append.map({ a -> JsonOutput.toJson(a.collect { File f -> f.absolutePath }) })
		}
	}

	@Override
	protected void exec()
	{
		def ext = Utils.getExt(project)

		environment "NODE_PATH", ext.toolsDirectory.dir("node_modules").get().asFile.absolutePath

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
