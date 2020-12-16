package com.innovenso.gradle.plugin.spring

import org.gradle.api.Plugin
import org.gradle.api.Project

class SpringBootPlugin implements Plugin<Project> {
	@Override
	void apply(Project project) {
		def innovensoSpring = project.extensions.create("innovensoSpring", SpringBootPluginExtension)

		project.plugins.apply('org.springframework.boot')
		project.plugins.apply('io.spring.dependency-management')

		project.configurations {
			all.collect { configuration ->
				configuration.exclude group: 'org.springframework.boot', module: 'spring-boot-starter-logging'
			}
		}

		project.dependencies {
			api 'org.springframework.boot:spring-boot-starter'
			api 'org.springframework.boot:spring-boot-starter-log4j2'
		}

		project.afterEvaluate {
			project.bootJar {
				enabled = innovensoSpring.bootJarEnabled
			}
		}
	}
}
