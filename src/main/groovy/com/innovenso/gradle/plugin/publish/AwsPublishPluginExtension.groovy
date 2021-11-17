package com.innovenso.gradle.plugin.publish

class AwsPublishPluginExtension {
	String awsAccessKeyId = System.getenv('AWS_ACCESS_KEY_ID')
	String awsSecretAccessKey = System.getenv('AWS_SECRET_ACCESS_KEY')
	String awsS3Bucket = System.getenv('INNOVENSO_PUBLISH_AWS_BUCKET')
	String awsS3BucketFolder = System.getenv('INNOVENSO_PUBLISH_AWS_FOLDER')
}
