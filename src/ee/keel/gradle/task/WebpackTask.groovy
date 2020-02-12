package ee.keel.gradle.task

import javax.inject.Inject

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.logging.LogLevel
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory

import ee.keel.gradle.BasePlugin
import ee.keel.gradle.Utils
import ee.keel.gradle.dsl.JsToolkitExtension
import ee.keel.gradle.dsl.WithEnvironmentProperties
import groovy.transform.CompileStatic

@CompileStatic
class WebpackTask extends NodeTask implements WithEnvironmentProperties
{
	private final static Logger logger = Logging.getLogger(WebpackTask)

	protected final JsToolkitExtension jstk = Utils.getExt(project)

	@Input
	final Property<String> module = project.objects.property(String)

	@Input
	final Property<String> env = project.objects.property(String)

	@Input
	final Property<String> browsersListEnv = project.objects.property(String).convention("production")

	@InputFile
	final RegularFileProperty config = project.objects.fileProperty()

	@InputFile
	final RegularFileProperty babelConfig = project.objects.fileProperty()

	@Input
	final Property<String> reactPragma = project.objects.property(String).convention(jstk.babel.map { it.reactPragma.get() })

	@Input
	final Property<Boolean> alwaysTranspile = project.objects.property(Boolean).convention(jstk.webpack.map { it.alwaysTranspile.get() })

	@Input
	final ListProperty<String> libraries = project.objects.listProperty(String).convention([])

	@Input
	final ListProperty<String> entries = project.objects.listProperty(String).convention([])

	@Input
	final Property<Boolean> preferModules = project.objects.property(Boolean).convention(true)

	@Input
	final Property<Boolean> minify = project.objects.property(Boolean).convention(true)

	@OutputDirectory
	final DirectoryProperty outputDirectory = project.objects.directoryProperty().convention(project.layout.buildDirectory)

	@OutputDirectory
	final DirectoryProperty recordsDirectory = project.objects.directoryProperty().convention(project.layout.buildDirectory.dir("records"))

	// output for library, input for module
	@Internal
	final DirectoryProperty manifestDirectory = project.objects.directoryProperty().convention(project.layout.buildDirectory.dir("manifest"))

	@Inject
	public WebpackTask()
	{
		super();

		configure {
			def jstk = Utils.getExt(getProject())
			def t = jstk.toolsDirectory.get()

			setWorkingDir(t.asFile.absoluteFile)

			args t.file("node_modules/webpack/bin/webpack.js").asFile.absolutePath

			if(project.logger.debugEnabled)
			{
				args '--display-exclude', '--display-modules', '--display-origins', '--display-reasons'

				environment "JSTK_DEBUG", "true"
			}

			environmentProperty "NODE_ENV", jstk.environment
			environmentProperty "ENV", env
			environmentProperty "MODULE", module
			environmentProperty "BROWSERSLIST_ENV", browsersListEnv
			environmentProperty "MINIFY", minify
			environmentProperty "PREFER_MODULES", preferModules
			environmentProperty "ALWAYS_TRANSPILE", alwaysTranspile
			environmentProperty "REACT_PRAGMA", reactPragma
		}
	}

	@Override
	protected void exec()
	{
		def t = jstk.toolsDirectory.get()

		applyEnvironmentProperties()

		environment "NODE_PATH", t.dir("node_modules").asFile.absolutePath
		environment 'BUILD_DIR', project.buildDir.absolutePath
		environment 'TOOLS_DIR', t.asFile.absolutePath
		environment 'PROJECT_DIR', project.projectDir.absolutePath
		environment 'BABEL_CACHE_DIR', new File(project.buildDir, ".babel").absolutePath
		environment 'OUTPUT_DIR', outputDirectory.asFile.get().absolutePath
		environment 'MANIFEST_DIR', manifestDirectory.asFile.get().absolutePath
		environment 'RECORDS_DIR', recordsDirectory.asFile.get().absolutePath

		environment "BABEL_CONFIG", babelConfig.asFile.get().absolutePath
		environment "LIBRARIES", libraries.get().join(",")
		environment "ENTRIES", entries.get().join(",")

		Map<String, String> presets = jstk.babel.get().presets.get()
		def preset = browsersListEnv.get()

		if(!presets.containsKey(preset))
		{
			throw new IllegalStateException("Preset ${preset} not found!")
		}

		environment "BROWSERSLIST", presets.get(preset)

		args '--config', config.get().asFile.absolutePath

		if(continuous.get())
		{
			args '--watch'
		}

		super.exec();
	}

	@Internal
	public LogLevel getStdoutLogLevel()
	{
		return LogLevel.DEBUG
	}

	@Internal
	public LogLevel getStdErrLogLevel()
	{
		return LogLevel.WARN
	}
}
