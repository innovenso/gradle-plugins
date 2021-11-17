package com.innovenso.gradle.plugin.publish

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.repositories.PasswordCredentials
import org.gradle.api.credentials.AwsCredentials
import org.gradle.api.publish.maven.MavenPublication

class OpenSourcePublishPlugin implements Plugin<Project> {

	@Override
	void apply(Project project) {
		def config = project.extensions.create('publishOSS', OpenSourcePublishPluginExtension)
		applyPlugins(project)


		project.afterEvaluate {
			if(hasGradlePlugin(project)) {
				applyGradlePublicationSource(project)
			} else {
				applyJavaPublicationSource(project)
			}

			applyPublicationRepositories(project)
		}
	}

	void applyPlugins(Project project) {
		project.plugins.apply('maven-publish')
		project.plugins.apply('signing')
	}

	void applyJavaPublicationSource(Project project) {
		def config = project.extensions.getByName('publishOSS')
		println "applying java publication source"
		project.publishing {
			publications {
				mavenJava(MavenPublication) { publication ->
					from project.components.java
					applyPom(project, publication)
				}
			}
		}

		project.signing {
			String signingKey = config.signingKey
			String signingPassword = config.signingPassword
			useInMemoryPgpKeys(signingKey, signingPassword)
			if (project.publishing.publications.mavenJava) {
				sign project.publishing.publications.mavenJava
			}
		}
	}

	void applyGradlePublicationSource(Project project) {
		def config = project.extensions.getByName('publishOSS')
		println "applying gradle publication source"
		project.publishing {
			publications {
				pluginMaven (MavenPublication) { publication ->
					applyPom(project, publication)
				}
			}
		}

		project.signing {
			String signingKey = config.signingKey
			String signingPassword = config.signingPassword
			useInMemoryPgpKeys(signingKey, signingPassword)
			if (project.publishing.publications.pluginMaven) {
				sign project.publishing.publications.pluginMaven
			}
		}
	}

	void applyPom(Project project, MavenPublication publication) {
		def config = project.extensions.getByName('publishOSS')
		publication.pom {
			name = project.name
			description = config.description
			if (config.projectUrl) {
				url = config.projectUrl
			}

			scm {
				connection = config.scmUrl
				developerConnection = config.scmDeveloperUrl
				url = config.projectSourceUrl
			}

			licenses {
				license {
					name = config.license
					url = config.licenseUrl
				}
			}

			developers {
				developer {
					id = config.maintainerId
					name = config.maintainerName
					email = config.maintainerEmail
				}
			}
		}
	}


	boolean hasGradlePlugin(Project project) {
		project.plugins.hasPlugin('groovy-gradle-plugin') || project.plugins.hasPlugin('java-gradle-plugin')
	}

	void applyPublicationRepositories(Project project) {
		def config = project.extensions.getByName('publishOSS')
		String ossrhUsername = config.ossrhUsername
		String ossrhPassword = config.ossrhPassword

		if (!ossrhUsername || !ossrhPassword) {
			project.logger.warn 'Open Source Repository Hosting username or password empty, skipping OSSRH publication'
		}

		project.publishing.repositories {
			maven {
				def releasesRepoUrl = "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
				def snapshotsRepoUrl = "https://s01.oss.sonatype.org/content/repositories/snapshots/"
				url project.version.toString().endsWith('SNAPSHOT') ? snapshotsRepoUrl : releasesRepoUrl
				credentials(PasswordCredentials) {
					username = ossrhUsername
					password = ossrhPassword
				}
			}
		}
	}
}
