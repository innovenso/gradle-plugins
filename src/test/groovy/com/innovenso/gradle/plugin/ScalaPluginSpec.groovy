package com.innovenso.gradle.plugin

import org.apache.commons.io.FileUtils
import org.gradle.testkit.runner.GradleRunner
import spock.lang.Specification
import spock.lang.TempDir
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class ScalaPluginSpec extends Specification {
	@TempDir File testProjectDir
	File buildFile
	File buildDir
	String version

	def setup() {
		buildDir = new File(testProjectDir, 'build')
		buildFile = new File(testProjectDir, 'build.gradle')
		version = '1.2.3-SNAPSHOT'
		buildFile << """
            plugins {
                id 'com.innovenso.gradle.scala'
            }
            
            version '${version}'
            
        """
	}

	def "twirl tasks are available"() {
		given:
		File templatesDir = testProjectDir.toPath().resolve('src/main/templates').toFile()
		templatesDir.mkdirs()
		copySample('twirl.sample', new File(templatesDir, 'hello.scala.html'))

		when:
		def result = GradleRunner.create()
				.withProjectDir(testProjectDir)
				.withArguments('compilePlayTwirlTemplates')
				.withPluginClasspath()
				.build()
		then:
		println result.output
		result.task(":compilePlayTwirlTemplates").outcome == SUCCESS
	}

	private void copySample(String source, File target) {
		if (!target.exists()) {
			final URL templateResource = this.getClass().getClassLoader().getResource("samples/" + source)
			FileUtils.copyURLToFile(templateResource, target)
		}
	}
}
