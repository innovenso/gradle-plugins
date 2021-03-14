package com.innovenso.gradle.plugin.java

import org.apache.commons.io.FileUtils
import org.gradle.api.Plugin
import org.gradle.api.Project

class JavaPlugin implements Plugin<Project> {
	@Override
	void apply(Project project) {
		project.extensions.create("innovensoJava", JavaPluginExtension)

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
				url 'https://innovenso-696563788450.d.codeartifact.eu-west-1.amazonaws.com/maven/innovenso/'
				credentials {
					username "aws"
					password System.env.CODEARTIFACT_AUTH_TOKEN
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
