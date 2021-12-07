package com.innovenso.gradle.plugin

import org.apache.commons.io.FileUtils
import org.gradle.testkit.runner.GradleRunner
import spock.lang.Specification
import spock.lang.TempDir

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class LatexPluginSpec extends Specification {
	@TempDir File testProjectDir
	File buildFile
	File buildDir
	File outputDir
	String version

	def setup() {
		outputDir = new File(testProjectDir, 'output')
		buildDir = new File(testProjectDir, 'build')
		buildFile = new File(testProjectDir, 'build.gradle')
		version = '1.2.3-SNAPSHOT'
		buildFile << """
            plugins {
                id 'com.innovenso.gradle.latex'
            }
            
            version '${version}'
            
        """
	}

	def "version file is created"() {
		when:
		def result = GradleRunner.create()
				.withProjectDir(testProjectDir)
				.withArguments('cleanLatex', 'writeVersionFile')
				.withPluginClasspath()
				.build()
		then:
		println result.output
		new File(buildDir, 'documentVersion.tex').text.contains(version)
		buildDir.listFiles().each {
			println it
		}
		//        result.task(":writeVersionFile").outcome == SUCCESS
	}

	def "images are copied to build directory"() {
		given:
		File imagesDir = testProjectDir.toPath().resolve('src/main/images').toFile()
		imagesDir.mkdirs()
		copySample('logo.png', new File(imagesDir, 'logo.png'))
		imagesDir.listFiles().each {println it}
		when:
		def result = GradleRunner.create()
				.withProjectDir(testProjectDir)
				.withArguments('copyLatexImages')
				.withPluginClasspath()
				.build()
		then:
		println result.output
		buildDir.toPath().resolve('images/logo.png').toFile().exists()
	}

	def "libraries are copied to build directory"() {
		given:
		File libDir = testProjectDir.toPath().resolve('src/main/lib').toFile()
		libDir.mkdirs()
		copySample('logo.png', new File(libDir, 'logo.png'))
		libDir.listFiles().each {println it}
		when:
		def result = GradleRunner.create()
				.withProjectDir(testProjectDir)
				.withArguments('copyLatexLibraries')
				.withPluginClasspath()
				.build()
		then:
		println result.output
		buildDir.toPath().resolve('lib/logo.png').toFile().exists()
	}

	def "themes are copied to build directory"() {
		given:
		File themeDir = testProjectDir.toPath().resolve('src/main/theme').toFile()
		themeDir.mkdirs()
		copySample('logo.png', new File(themeDir, 'logo.png'))
		themeDir.listFiles().each {println it}
		when:
		def result = GradleRunner.create()
				.withProjectDir(testProjectDir)
				.withArguments('copyBeamerThemes')
				.withPluginClasspath()
				.build()
		then:
		println result.output
		buildDir.toPath().resolve('theme/logo.png').toFile().exists()
	}

	def "source files are copied to build directory"() {
		given:
		File sourceDir = testProjectDir.toPath().resolve('src/main/latex').toFile()
		sourceDir.mkdirs()
		copySample('document.sample', new File(sourceDir, 'document.tex'))
		sourceDir.listFiles().each {println it}
		when:
		def result = GradleRunner.create()
				.withProjectDir(testProjectDir)
				.withArguments('copyLatexSource')
				.withPluginClasspath()
				.build()
		then:
		println result.output
		buildDir.toPath().resolve('document.tex').toFile().exists()
	}

	def "document is compiled to PDF"() {
		given:
		File sourceDir = testProjectDir.toPath().resolve('src/main/latex').toFile()
		sourceDir.mkdirs()
		copySample('document.sample', new File(sourceDir, 'document.tex'))
		copySample('document.sample', new File(sourceDir, 'handouts.tex'))
		copySample('document.sample', new File(sourceDir, 'presentation.tex'))
		copySample('document.sample', new File(sourceDir, 'handouts_with_notes.tex'))
		when:
		def result = GradleRunner.create()
				.withProjectDir(testProjectDir)
				.withArguments('pdf')
				.withPluginClasspath()
				.build()
		then:
		println result.output
		outputDir.listFiles().size() == 4
		outputDir.listFiles().each {println it }
		result.task(":pdf").outcome == SUCCESS
	}

	private void copySample(String source, File target) {
		if (!target.exists()) {
			final URL templateResource = this.getClass().getClassLoader().getResource("samples/" + source)
			FileUtils.copyURLToFile(templateResource, target)
		}
	}
}
