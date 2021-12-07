package com.innovenso.gradle.plugin.scala

import com.innovenso.gradle.plugin.latex.LatexPluginExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class ScalaPlugin implements Plugin<Project> {
	@Override
	void apply(Project project) {
		ScalaPluginExtension config = project.extensions.create('innovensoScala', ScalaPluginExtension)

		project.plugins.apply('scala')
		project.plugins.apply('groovy')
		project.plugins.apply('org.gradle.playframework')

		project.repositories {
			mavenCentral()
			mavenLocal()
			maven {
				url 'https://download.innovenso.io/maven'
			}

			maven {
				name "lightbend-maven-release"
				url "https://repo.lightbend.com/lightbend/maven-releases"
			}
			ivy {
				name "lightbend-ivy-release"
				url "https://repo.lightbend.com/lightbend/ivy-releases"
				layout "ivy"
			}
		}

		project.sourceSets {
			main {
				scala {
					srcDir 'src/main/scala'
				}
				twirl {
					srcDir 'src/main/templates'
				}
			}
		}
	}
}
