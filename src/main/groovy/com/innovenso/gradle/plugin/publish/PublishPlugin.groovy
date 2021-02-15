package com.innovenso.gradle.plugin.publish

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.internal.publication.DefaultMavenPublication

class PublishPlugin implements Plugin<Project> {

	@Override
	void apply(Project project) {
		applyPlugins(project)

		applyJavaPublicationSource(project)
	}

	void applyPlugins(Project project) {
		project.plugins.apply('maven-publish')
	}

	void applyJavaPublicationSource(Project project) {
		println "applying java publication source"
		project.publishing {
			publications {
				mavenJava(MavenPublication) {
					from project.components.java
				}
			}
			repositories {
				maven {
					url 'https://innovenso-696563788450.d.codeartifact.eu-west-1.amazonaws.com/maven/innovenso/'
					credentials {
						username "aws"
						password System.env.CODEARTIFACT_AUTH_TOKEN
					}
				}
			}
		}
	}
}
