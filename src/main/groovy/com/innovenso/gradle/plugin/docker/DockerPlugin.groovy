package com.innovenso.gradle.plugin.docker

import org.apache.commons.io.FileUtils
import org.apache.tools.ant.taskdefs.ExecTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Exec

class DockerPlugin implements Plugin<Project> {
	@Override
	void apply(Project project) {
		project.extensions.create("innovensoDocker", DockerPluginExtension)

		project.afterEvaluate {
			def tag = project.innovensoDocker.tag ?: project.name
			def version = project.version

			project.task(Map.of("type", Copy, "group", "Docker"), 'copyJar') {
				from project.jar // here it automatically reads jar file produced from jar task
				into project.buildDir
				rename { filename -> innovensoDocker.jarFileName}
			}

			project.copyJar.dependsOn('jar')
			project.build.dependsOn('copyJar')

			project.task(Map.of("type", Exec, "group", "Docker"), 'vctlBuild') {
				workingDir "${project.projectDir}"
				commandLine 'vctl', 'build', '--builder-mem', '8g', '--tag', "${tag}:${version}" , '.'
			}

			project.task(Map.of("type", Exec, "group", "Docker"), 'dockerBuild') {
				workingDir "${project.projectDir}"
				commandLine 'docker', 'build', '--memory', '8g', '--tag', "${tag}:${version}" , '.'
			}

			project.task(Map.of("type", Exec, "group", "Docker"), 'vctlTag') {
				workingDir "${project.projectDir}"
				commandLine 'vctl', 'tag', '-f', "${tag}:${version}" , tag
			}

			project.task(Map.of("type", Exec, "group", "Docker"), 'dockerTag') {
				workingDir "${project.projectDir}"
				commandLine 'docker', 'tag', "${tag}:${version}" , tag
			}

			project.task(Map.of("type", Exec, "group", "Docker"), 'vctlPush') {
				workingDir "${project.projectDir}"
				commandLine 'vctl', 'push', '-u', System.getenv('DOCKER_USERNAME'), '-p', System.getenv('DOCKER_PASSWORD'), "${tag}:${version}"
			}

			project.task(Map.of("type", Exec, "group", "Docker"), 'dockerPush') {
				workingDir "${project.projectDir}"
				commandLine 'docker', 'push', "${tag}:${version}"
			}

			project.task(Map.of("group", "Docker"), 'vctl')
			project.task(Map.of("group", "Docker"), 'docker')
			project.vctl.dependsOn('vctlBuild', 'vctlTag', 'vctlPush')
			project.docker.dependsOn('dockerBuild', 'dockerTag', 'dockerPush')

		}
	}

	private void copyTemplate(String source, File target) {
		final URL templateResource = this.getClass().getClassLoader().getResource("templates/" + source)
		FileUtils.copyURLToFile(templateResource, target);
	}

}
