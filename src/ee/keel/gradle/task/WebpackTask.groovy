package ee.keel.gradle.task;

import groovy.transform.CompileStatic

@CompileStatic
public class WebpackTask extends AbstractWebpackTask
{
	public WebpackTask()
	{
		super()

		doLast { t ->
			new File(project.buildDir, "webpack."+this.module.get()+".env").text = getEnvironment().collect { k, v -> k+'="'+String.valueOf(v).replaceAll(/"/, '\"')+'"' }.join("\n")
		}

		configure {
			args jstk.toolsDirectory.get().file("node_modules/webpack/bin/webpack.js").asFile.absolutePath

			if(project.logger.debugEnabled)
			{
				args '--stats-errors', '--stats-error-details', '--stats-error-stack', '--stats-chunks', '--stats-modules', '--stats-reasons', '--stats-warnings', '--stats-assets'
			}
		}
	}

	@Override
	protected void exec0()
	{
		super.exec0();

		args '--config', config.get().asFile.absolutePath

		if(continuous.get())
		{
			args '--watch'
		}

		environment "RESOLVE_FILE", new File(project.buildDir, "webpack."+module.get()+".js")
	}
}
