package org.unbrokendome.gradle.plugins.helm.release.rules

import org.gradle.api.NamedDomainObjectCollection
import org.gradle.api.Task
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskDependency
import org.unbrokendome.gradle.plugins.helm.HELM_GROUP
import org.unbrokendome.gradle.plugins.helm.release.dsl.HelmRelease
import org.unbrokendome.gradle.plugins.helm.release.dsl.HelmReleaseTarget
import org.unbrokendome.gradle.plugins.helm.release.dsl.shouldInclude
import org.unbrokendome.gradle.plugins.helm.rules.AbstractTaskRule
import org.unbrokendome.gradle.plugins.helm.rules.RuleNamePattern


private val namePattern =
    RuleNamePattern.parse("helmInstallTo<Target>")


internal fun installAllToTargetTaskName(targetName: String) =
    namePattern.mapName(targetName)


/**
 * Creates a task to install all releases to a given release target.
 */
internal class HelmInstallToTargetTaskRule(
    tasks: TaskContainer,
    private val releases: NamedDomainObjectCollection<HelmRelease>,
    releaseTargets: NamedDomainObjectCollection<HelmReleaseTarget>
) : AbstractTaskRule<HelmReleaseTarget, Task>(Task::class.java, tasks, releaseTargets, namePattern) {


    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override fun Task.configureFrom(releaseTarget: HelmReleaseTarget) {

        group = HELM_GROUP
        description = "Installs or upgrades all matching releases to the \"${releaseTarget.name}\" target."

        dependsOn(TaskDependency {
            releases
                .matching { release ->
                    releaseTarget.shouldInclude(release)
                }
                .names
                .mapNotNull { releaseName ->
                    val installReleaseTaskName = releaseTarget.installReleaseTaskName(releaseName)
                    project.tasks.findByName(installReleaseTaskName)
                }
                .toSet()
        })
    }
}
