package com.innovenso.gradle.plugin.docker

class DockerPluginExtension {
	String tag
	String jarFileName = 'application.jar'
	String target = 'docker'
}
