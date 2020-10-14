package com.innovenso.gradle.plugin.publish

import org.gradle.api.Plugin
import org.gradle.api.Project
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
		applyTaskDependencies(project)
	}

	void applyPlugins(Project project) {
		project.plugins.apply('maven-publish')
		project.plugins.apply('com.jfrog.artifactory')
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
		project.artifactory {
			contextUrl = System.getenv('ARTIFACTORY_CONTEXTURL')   //The base Artifactory URL if not overridden by the publisher/resolver
			publish {
				repository {
					repoKey = 'innovenso'
					username = System.getenv("ARTIFACTORY_USERNAME")
					password = System.getenv("ARTIFACTORY_PASSWORD")
					maven = true
				}
				defaults {
					publications('mavenJava', 'pluginPublication')
				}
			}
			resolve {
				repository {
					repoKey = 'repo'
					username = System.getenv("ARTIFACTORY_USERNAME")
					password = System.getenv("ARTIFACTORY_PASSWORD")
					maven = true

				}
			}
		}
	}

	void applyTaskDependencies(Project project) {
		project.publish.dependsOn('artifactoryPublish')
	}
}
