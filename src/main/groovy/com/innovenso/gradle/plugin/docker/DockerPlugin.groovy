package com.innovenso.gradle.plugin.docker

import org.apache.tools.ant.taskdefs.ExecTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Exec

class DockerPlugin implements Plugin<Project> {
	@Override
	void apply(Project project) {
		def innovensoDocker = project.extensions.create("innovensoDocker", DockerPluginExtension)

		project.afterEvaluate {
			project.task(Map.of("type", Copy, "group", "Docker"), 'copyJar') {
				from project.jar // here it automatically reads jar file produced from jar task
				into project.buildDir
				rename { filename -> innovensoDocker.jarFileName}
			}

			project.copyJar.dependsOn('jar')
			project.build.dependsOn('copyJar')

			project.task(Map.of("type", Exec, "group", "Docker"), 'vctlBuild') {
				workingDir "${project.projectDir}"
				commandLine 'vctl', 'build', '--builder-mem', '8g', '--tag', "${innovensoDocker.tag}:${project.version}" , '.'
			}

			project.task(Map.of("type", Exec, "group", "Docker"), 'dockerBuild') {
				workingDir "${project.projectDir}"
				commandLine 'docker', 'build', '--memory', '8g', '--tag', "${innovensoDocker.tag}:${project.version}" , '.'
			}

			project.task(Map.of("type", Exec, "group", "Docker"), 'vctlTag') {
				workingDir "${project.projectDir}"
				commandLine 'vctl', 'tag', '-f', "${innovensoDocker.tag}:${project.version}" , innovensoDocker.tag
			}

			project.task(Map.of("type", Exec, "group", "Docker"), 'dockerTag') {
				workingDir "${project.projectDir}"
				commandLine 'docker', 'tag', "${innovensoDocker.tag}:${project.version}" , innovensoDocker.tag
			}

			project.task(Map.of("type", Exec, "group", "Docker"), 'vctlPush') {
				workingDir "${project.projectDir}"
				commandLine 'vctl', 'push', '-u', System.getenv('DOCKER_USERNAME'), '-p', System.getenv('DOCKER_PASSWORD'), "${innovensoDocker.tag}:${project.version}"
			}

			project.task(Map.of("type", Exec, "group", "Docker"), 'dockerPush') {
				workingDir "${project.projectDir}"
				commandLine 'docker', 'push', "${innovensoDocker.tag}:${project.version}"
			}

			project.task('vctl')
			project.task('docker')
			project.vctl.dependsOn('vctlBuild', 'vctlTag', 'vctlPush')
			project.docker.dependsOn('dockerBuild', 'dockerTag', 'dockerPush')

		}
	}
}
