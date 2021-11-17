package com.innovenso.gradle.plugin.java

import org.apache.commons.io.FileUtils
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.jvm.tasks.Jar

class JavaPlugin implements Plugin<Project> {
	@Override
	void apply(Project project) {
		def	awsAccessKeyId = System.getenv('AWS_ACCESS_KEY_ID') ?: project.findProperty('aws_access_key_id')
		def	awsSecretAccessKey = System.getenv('AWS_SECRET_ACCESS_KEY') ?: project.findProperty('aws_secret_access_key')

		project.extensions.create("innovensoJava", JavaPluginExtension)

		project.plugins.apply('java-library')
		project.plugins.apply('groovy')
		project.plugins.apply('idea')
		//		project.plugins.apply('jacoco')
		project.plugins.apply('io.freefair.lombok')
		project.plugins.apply('com.diffplug.spotless')

		project.repositories {
			mavenCentral()
			mavenLocal()
			maven {
				url 'https://download.innovenso.io/maven'
			}
		}

		project.idea {
			module {
				downloadJavadoc = true
				downloadSources = true
			}
		}


		project.dependencies {
			testImplementation "org.codehaus.groovy:groovy:3.0.7"
			testImplementation "org.codehaus.groovy:groovy-json:3.0.7"
			testImplementation "org.spockframework:spock-core:2.0-M4-groovy-3.0"
		}

		project.lombok {
			config['lombok.nonNull.exceptionType'] = 'IllegalArgumentException'
			config['lombok.toString.includeFieldNames'] = "false"
			config['lombok.toString.callSuper'] = "SKIP"
		}

		project.spotless {
			java {
				eclipse()
			}
			groovy {
				greclipse()
			}
		}

		project.test {
			useJUnitPlatform()
		}

		project.java {
			withJavadocJar()
			withSourcesJar()
		}

		project.afterEvaluate {
			project.sourceCompatibility = project.innovensoJava.sourceCompatibility
			project.targetCompatibility = project.innovensoJava.targetCompatibility
			def basePackage = project.innovensoJava.basePackage ?: "${project.group}"
			def basePackageDir = basePackage.replaceAll('.', '/')

			project.task("initJava") {
				project.mkdir "src/main/java/$basePackageDir"
				project.mkdir 'src/main/resources'
				project.mkdir "src/test/groovy/$basePackageDir"
				project.mkdir 'src/test/resources'
			}
		}
	}

	private void copyTemplate(String source, File target) {
		final URL templateResource = this.getClass().getClassLoader().getResource("templates/" + source)
		FileUtils.copyURLToFile(templateResource, target);
	}
}
