package com.innovenso.gradle.plugin.docker

import org.apache.tools.ant.taskdefs.ExecTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Exec

class DockerPlugin implements Plugin<Project> {
	@Override
	void apply(Project project) {
		project.extensions.create("innovensoDocker", DockerPluginExtension)

		project.task(Map.of("type", org.gradle.api.tasks.Copy, "group", "Docker"), 'copyJar') {
			from project.jar // here it automatically reads jar file produced from jar task
			into project.buildDir
			rename { filename -> project.innovensoDocker.jarFileName}
		}

		project.copyJar.dependsOn('jar')
		project.build.dependsOn('copyJar')

		project.task(Map.of("type", Exec, "group", "Docker"), 'vctlBuild') {
			workingDir "${project.projectDir}"
			commandLine 'vctl', 'build', '--builder-mem', '8g', '--tag', "${project.innovensoDocker.tag}:${project.version}" , '.'
		}

		project.task(Map.of("type", Exec, "group", "Docker"), 'dockerBuild') {
			workingDir "${project.projectDir}"
			commandLine 'docker', 'build', '--memory', '8g', '--tag', "${project.innovensoDocker.tag}:${project.version}" , '.'
		}

		project.task(Map.of("type", Exec, "group", "Docker"), 'vctlTag') {
			workingDir "${project.projectDir}"
			commandLine 'vctl', 'tag', '-f', "${project.innovensoDocker.tag}:${project.version}" , project.innovensoDocker.tag
		}

		project.task(Map.of("type", Exec, "group", "Docker"), 'dockerTag') {
			workingDir "${project.projectDir}"
			commandLine 'docker', 'tag', "${project.innovensoDocker.tag}:${project.version}" , project.innovensoDocker.tag
		}

		project.task(Map.of("type", Exec, "group", "Docker"), 'vctlPush') {
			workingDir "${project.projectDir}"
			commandLine 'vctl', 'push', '-u', System.getenv('DOCKER_USERNAME'), '-p', System.getenv('DOCKER_PASSWORD'), "${project.innovensoDocker.tag}:${project.version}"
		}

		project.task(Map.of("type", Exec, "group", "Docker"), 'dockerPush') {
			workingDir "${project.projectDir}"
			commandLine 'docker', 'push', "${project.innovensoDocker.tag}:${project.version}"
		}

		project.task('vctl')
		project.task('docker')
		project.vctl.dependsOn('vctlBuild', 'vctlTag', 'vctlPush')
		project.docker.dependsOn('dockerBuild', 'dockerTag', 'dockerPush')
	}
}
