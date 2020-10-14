package com.innovenso.gradle.plugin.version

import org.ajoberstar.grgit.Grgit
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.LogLevel

/**
 * Created by jlust on 3/03/15.
 */
class VersioningPlugin implements Plugin<Project> {
	@Override
	void apply(Project project) {
		project.version = versionFromGitRepository(project)
		if (project.getDepth() == 0)
			println "project version from Git: ${project.version}"
	}

	Version versionFromGitRepository(Project project) {
		try {
			Grgit grgit = Grgit.open(dir: project.rootDir)
			List<Version> allVersions = assembleVersionsFromTagsOnCurrentBranch(grgit, project)
			if (allVersions) {
				return allVersions.sort().reverse()[0]
			} else {
				return new Version()
			}
		} catch (Exception grgitException) {
			if (project.logger.isEnabled(LogLevel.DEBUG)) {
				project.logger.debug("GrGit Error", grgitException)
			}
			project.logger.warn('could not determine version from Git repo, using default')
			return new Version()
		}
	}

	List<Version> assembleVersionsFromTagsOnCurrentBranch(Grgit repository, Project project) {
		List<String> tags = repository.tag.list()*.fullName
		String classifier = classifierFromBranch(repository)
		Version.Type versionType = versionTypeFromBranch(repository, project)

		return tags.collect { tag -> new Version(tag, classifier, versionType) }
	}

	String classifierFromBranch(Grgit repository) {
		String branchName = repository.branch.current().fullName - 'refs/heads/'
		if (branchName.startsWith('feature/') || branchName.startsWith('hotfix/')) return branchName - 'hotfix/' - 'feature/'
		else if (branchName.startsWith('release/')) return 'RC'
		else return null
	}

	Version.Type versionTypeFromBranch(Grgit repository, Project project) {
		String branchName = repository.branch.current().fullName - 'refs/heads/'

		if (project.getDepth() == 0)
			println "BRANCH: ${branchName}"

		if (branchName.startsWith('release/')) return Version.Type.PRERELEASE
		if (branchName == 'master') return Version.Type.RELEASE
		if (branchName.startsWith('hotfix/')) return Version.Type.HOTFIX
		if (branchName.startsWith('feature/')) return Version.Type.FEATURE
		else return Version.Type.DEVELOPMENT
	}
}
