package com.innovenso.gradle.plugin.spring

import org.gradle.api.Plugin
import org.gradle.api.Project

class SpringBootPlugin implements Plugin<Project> {
	@Override
	void apply(Project project) {
		def innovensoSpring = project.extensions.create("innovensoSpring", SpringBootPluginExtension)

		project.plugins.apply('org.springframework.boot')
		project.plugins.apply('io.spring.dependency-management')

		project.dependencies {
			api 'org.springframework.boot:spring-boot-starter'
		}

		project.afterEvaluate {
			project.bootJar {
				enabled = innovensoSpring.bootJarEnabled
			}
		}
	}
}
