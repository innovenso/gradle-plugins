package com.innovenso.gradle.plugin.publish

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.credentials.AwsCredentials
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.internal.publication.DefaultMavenPublication

class PublishPlugin implements Plugin<Project> {

	@Override
	void apply(Project project) {
		applyPlugins(project)

		if(hasGradlePlugin(project)) {
			applyGradlePublicationSource(project)
		} else {
			applyJavaPublicationSource(project)
		}
		applyPublicationRepositories(project)
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
		}
	}

	void applyGradlePublicationSource(Project project) {
		println "applying gradle publication source"
		project.publishing {
			publications {
				pluginPublication (MavenPublication) {
					from project.components.java
				}
			}
		}
	}

	boolean hasGradlePlugin(Project project) {
		project.plugins.hasPlugin('groovy-gradle-plugin') || project.plugins.hasPlugin('java-gradle-plugin')
	}

	void applyPublicationRepositories(Project project) {
		def	awsAccessKeyId = System.getenv('AWS_ACCESS_KEY_ID') ?: project.findProperty('aws_access_key_id')
		def	awsSecretAccessKey = System.getenv('AWS_SECRET_ACCESS_KEY') ?: project.findProperty('aws_secret_access_key')

		project.publishing.repositories {
			maven {
				url 's3://innovenso-io-website/maven/'
				credentials(AwsCredentials) {
					accessKey = awsAccessKeyId
					secretKey = awsSecretAccessKey
				}
			}
		}
	}
}
