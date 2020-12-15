package com.innovenso.gradle.plugin.spring

import org.gradle.api.Plugin
import org.gradle.api.Project

class SpringBootPlugin implements Plugin<Project> {
	@Override
	void apply(Project project) {
		project.extensions.create("innovensoSpring", SpringBootPluginExtension)

		project.plugins.apply('org.springframework.boot')
		project.plugins.apply('io.spring.dependency-management')

		project.configurations {
			all.collect { configuration ->
				configuration.exclude group: 'org.springframework.boot', module: 'spring-boot-starter-logging'
			}
		}

		project.bootJar {
			enabled = project.innovensoSpring.bootJarEnabled
		}

		project.dependencies {
			api 'org.springframework.boot:spring-boot-starter'
			api 'org.springframework.boot:spring-boot-starter-log4j2'
		}
	}
}
