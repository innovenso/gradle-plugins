package com.innovenso.gradle.plugin.publish

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.repositories.PasswordCredentials
import org.gradle.api.credentials.AwsCredentials
import org.gradle.api.publish.maven.MavenPublication

class AwsPublishPlugin implements Plugin<Project> {

	@Override
	void apply(Project project) {
		project.extensions.create('publishAWS', AwsPublishPluginExtension)
		applyPlugins(project)

		if(hasGradlePlugin(project)) {
			applyGradlePublicationSource(project)
		} else {
			applyJavaPublicationSource(project)
		}

		project.afterEvaluate {
			applyPublicationRepositories(project)
		}
	}

	static void applyPlugins(Project project) {
		project.plugins.apply('maven-publish')
	}

	void applyJavaPublicationSource(Project project) {
		println "applying java publication source"
		project.publishing {
			publications {
				mavenJava(MavenPublication) { publication ->
					from project.components.java
				}
			}
		}
	}

	void applyGradlePublicationSource(Project project) {
		println "applying gradle publication source"
		project.publishing {
			publications {
				pluginMaven (MavenPublication) {
					//artifact project.sourcesJar
				}
			}
		}
	}

	static boolean hasGradlePlugin(Project project) {
		project.plugins.hasPlugin('groovy-gradle-plugin') || project.plugins.hasPlugin('java-gradle-plugin')
	}

	void applyPublicationRepositories(Project project) {
		AwsPublishPluginExtension config = project.extensions.getByName('publishAWS') as AwsPublishPluginExtension
		String awsAccessKeyId = config.awsAccessKeyId
		String awsSecretAccessKey = config.awsSecretAccessKey
		String bucket = config.awsS3Bucket
		String folder = config.awsS3BucketFolder
		if (!awsAccessKeyId || !awsSecretAccessKey || !bucket) {
			project.logger.warn 'AWS Access Key, Secret Key or bucket name not configured, skipping AWS publication'
		}
		def s3DeployUrl = "s3://${bucket}/${folder}"
		project.publishing.repositories {
			maven {
				url s3DeployUrl
				credentials(AwsCredentials) {
					accessKey = awsAccessKeyId
					secretKey = awsSecretAccessKey
				}
			}
		}
	}
}
