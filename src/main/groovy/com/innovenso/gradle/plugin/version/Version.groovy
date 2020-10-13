package com.innovenso.gradle.plugin.version

import groovy.transform.Sortable

/**
 * Created by jlust on 3/03/15.
 */
@Sortable(includes = ['major', 'minor', 'patch'])
class Version {
    final Integer major
    final Integer minor
    final Integer patch
    final String classifier
    final Type versionType

    Version(String version, String classifier, Type versionType) {
        List<Integer> versionNumbers = version.findAll( /\d+/ )*.toInteger()
        this.major = versionNumbers[0] ?: 0
        Integer minorVersion = versionNumbers[1] ?: 0
        Integer patchVersion = versionNumbers[2] ?: 0
        this.classifier = classifier
        this.versionType = versionType
        if (versionType in [Type.FEATURE, Type.DEVELOPMENT, Type.PRERELEASE]) {
            this.minor = minorVersion + 1
            this.patch = 0
        } else if (versionType == Type.HOTFIX) {
            this.minor = minorVersion
            this.patch = patchVersion + 1
        } else {
            this.minor = minorVersion
            this.patch = patchVersion
        }
    }

    Version() {
        this('0.0.0',null,Type.DEVELOPMENT)
    }

    @Override
    public String toString() {
        StringBuilder versionBuilder = new StringBuilder("${major}.${minor}.${patch}")

        if (classifier) versionBuilder.append("-${classifier}")
        if (versionType in [Type.DEVELOPMENT, Type.FEATURE, Type.HOTFIX]) versionBuilder.append("-SNAPSHOT")
        return versionBuilder.toString()
    }

    public enum Type {
        RELEASE, PRERELEASE, HOTFIX, FEATURE, DEVELOPMENT
    }
}
