package com.innovenso.gradle.plugin.java

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.LogLevel

class JavaPlugin implements Plugin<Project> {
	@Override
	void apply(Project project) {
		project.extensions.create("innovensoJava", JavaPluginExtension)

		project.plugins.apply('java-library')
		project.plugins.apply('groovy')
		project.plugins.apply('idea')
		project.plugins.apply('jacoco')
		project.plugins.apply('io.freefair.lombok')
		project.plugins.apply('com.diffplug.spotless')

		project.sourceCompatibility = project.innovensoJava.sourceCompatibility
		project.targetCompatibility = project.innovensoJava.targetCompatibility

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
			testImplementation "org.codehaus.groovy:groovy-all:3.0.4"
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
	}
}
