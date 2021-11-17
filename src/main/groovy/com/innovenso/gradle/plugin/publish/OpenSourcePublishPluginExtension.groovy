package com.innovenso.gradle.plugin.publish

class OpenSourcePublishPluginExtension {
	String signingKey = System.getenv('INNOVENSO_PUBLISH_SIGNING_KEY')
	String signingPassword = System.getenv('INNOVENSO_PUBLISH_SIGNING_PASSWORD')
	String ossrhUsername = System.getenv('INNOVENSO_PUBLISH_OSSRH_USERNAME')
	String ossrhPassword = System.getenv('INNOVENSO_PUBLISH_OSSRH_PASSWORD')
	String description
	String projectUrl
	String scmUrl
	String scmDeveloperUrl
	String projectSourceUrl
	String license
	String licenseUrl
	String maintainerName
	String maintainerId
	String maintainerEmail
}
