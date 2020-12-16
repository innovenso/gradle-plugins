package com.innovenso.gradle.plugin.java

import org.apache.commons.io.FileUtils
import org.gradle.api.Plugin
import org.gradle.api.Project

class JavaPlugin implements Plugin<Project> {
	@Override
	void apply(Project project) {
		def innovensoJava = project.extensions.create("innovensoJava", JavaPluginExtension)

		project.plugins.apply('java-library')
		project.plugins.apply('groovy')
		project.plugins.apply('idea')
		//		project.plugins.apply('jacoco')
		project.plugins.apply('io.freefair.lombok')
		project.plugins.apply('com.diffplug.spotless')

		project.repositories {
			mavenCentral()
			jcenter()
			mavenLocal()
			maven {
				url 'https://innovenso.jfrog.io/artifactory/repo/'
				credentials {
					username = System.getenv("ARTIFACTORY_USERNAME")
					password = System.getenv("ARTIFACTORY_PASSWORD")
				}
			}
		}

		project.idea {
			module {
				downloadJavadoc = true
				downloadSources = true
			}
		}


		project.dependencies {
			implementation group: 'org.apache.logging.log4j', name: 'log4j-api', version: '2.12.1'
			implementation group: 'org.apache.logging.log4j', name: 'log4j-core', version: '2.12.1'
			testImplementation "org.codehaus.groovy:groovy:3.0.4"
			testImplementation "org.spockframework:spock-core:2.0-M3-groovy-3.0"
		}

		project.lombok {
			config['lombok.nonNull.exceptionType'] = 'IllegalArgumentException'
			config['lombok.toString.includeFieldNames'] = "false"
			config['lombok.toString.callSuper'] = "SKIP"
		}

		project.spotless {
			java {
				googleJavaFormat()
			}
			groovy {
				greclipse()
			}
		}

		project.test {
			useJUnitPlatform()
		}

		project.afterEvaluate {
			project.sourceCompatibility = innovensoJava.sourceCompatibility
			project.targetCompatibility = innovensoJava.targetCompatibility

			project.task("initJava") {
				def basePackageDir = innovensoJava.basePackage.replaceAll('.', '/')
				project.mkdir "src/main/java/$basePackageDir"
				project.mkdir 'src/main/resources'
				project.mkdir "src/test/groovy/$basePackageDir"
				project.mkdir 'src/test/resources'

				copyTemplate('gitignore', project.file('.gitignore'))
				copyTemplate('log4j2.xml', project.file('src/test/resources/log4j2.xml'))
			}
		}


	}

	private static void copyTemplate(String source, File target) {
		final URL templateResource = this.getClass().getClassLoader().getResource("templates/" + source)
		FileUtils.copyURLToFile(templateResource, target);
	}
}
