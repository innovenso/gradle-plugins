package com.innovenso.gradle.plugin.latex

import org.apache.commons.io.FileUtils
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Exec

import java.nio.file.Path

class LatexPlugin implements Plugin<Project> {
	@Override
	void apply(Project project) {
		LatexPluginExtension latex = project.extensions.create('latex', LatexPluginExtension)

		project.afterEvaluate {

			project.tasks.register('writeVersionFile') {
				group = 'LaTeX'
				description = 'writes the project version to a tex file so it can be included in the document'
				writeFileInBuildDirectory(project, latex.versionFileName, "\\version{${project.version}}")
			}

			project.tasks.register('copyLatexImages', Copy) {
				group = 'LaTeX'
				from getDirectory(project, latex.imagesDirectory)
				into new File(project.buildDir, 'images')
				doFirst {
					maybeCreateDirectory(project, 'build/images')
				}
			}

			project.tasks.register('copyLatexLibraries', Copy) {
				group = 'LaTeX'
				from getDirectory(project, latex.libDirectory)
				into new File(project.buildDir, 'lib')
				doFirst {
					maybeCreateDirectory(project, 'build/lib')
				}
			}

			project.tasks.register('copyBeamerThemes', Copy) {
				group = 'LaTeX'
				from getDirectory(project, latex.themeDirectory)
				into new File(project.buildDir, 'theme')
				doFirst {
					maybeCreateDirectory(project, 'build/theme')
				}
			}

			project.tasks.register('copyLatexSource', Copy) {
				group = 'LaTeX'
				from getDirectory(project, latex.sourceDirectory)
				into project.buildDir
			}

			project.tasks.register('prepareLatexBuild') {
				dependsOn 'writeVersionFile', 'copyLatexImages', 'copyLatexLibraries', 'copyBeamerThemes', 'copyLatexSource'
				group = 'LaTeX'
				description = 'prepares the build directory for running LaTeX'
				maybeCreateDirectory(project, latex.outputDirectory)
			}

			project.tasks.register('cleanLatex') {
				group = 'LaTeX'
				description = 'prepares the build directory for running LaTeX'
				maybeRemoveDirectory(project, latex.outputDirectory)
				maybeRemoveDirectory(project, project.buildDir.name)
			}

			registerBuildTask(project, 'buildPdfDocument', latex.documentMainFileName, 'renders the document to PDF.', 'document')
			registerBuildTask(project, 'buildPdfHandout', latex.handoutMainFileName, 'renders the Beamer handout (if any) to PDF.', 'handout')
			registerBuildTask(project, 'buildPdfPresentation', latex.presentationMainFileName, 'renders the Beamer presentation (if any) to PDF.', 'presentation')
			registerBuildTask(project, 'buildPdfNotes', latex.handoutWithNotesMainFileName, 'renders the Beamer handout with notes (if any) to PDF.', 'notes')

			project.tasks.register('pdf') {
				dependsOn 'buildPdfDocument', 'buildPdfHandout', 'buildPdfPresentation', 'buildPdfNotes'
				group = 'LaTeX'
				description = 'Renders all PDF files to the output directory'
			}
		}
	}

	static void registerBuildTask(Project project, String taskName, String inputFileName, String taskDescription, String outputFileSuffix) {
		project.tasks.register(taskName) {
			dependsOn 'prepareLatexBuild'
			group = 'LaTeX'
			description = taskDescription

			doLast {
				File mainFile = new File(project.buildDir, inputFileName + '.tex')
				if (mainFile.canRead()) {
					println "building ${inputFileName}.tex"

					project.exec {
						workingDir = project.buildDir
						commandLine 'latexmk', '-pdf', '-interaction=nonstopmode', "${inputFileName}.tex"
					}

					LatexPluginExtension latex = project.extensions.getByName('latex') as LatexPluginExtension
					File targetFile = new File(getDirectory(project, latex.outputDirectory), "${project.name}-${outputFileSuffix}-${project.version}.pdf")
					FileUtils.copyFile(new File(project.buildDir, inputFileName + '.pdf'), targetFile)
				}
			}
		}
	}

	static void writeFileInBuildDirectory(Project project, String filename, String text) {
		if (!project.buildDir.exists()) {
			project.buildDir.mkdirs()
		}
		File outputFile = new File(project.buildDir, filename)
		outputFile << text
	}

	static void maybeCreateDirectory(Project project, String directoryName) {
		File directoryToCreate = getDirectory(project, directoryName)
		if (!directoryToCreate.exists()) {
			directoryToCreate.mkdirs()
		}
	}

	static void maybeRemoveDirectory(Project project, String directoryName) {
		getDirectory(project, directoryName).with {
			project.delete(it)
		}
	}

	static File getDirectory(Project project, String directoryName) {
		project.projectDir.toPath().resolve(directoryName).toFile()
	}
}
