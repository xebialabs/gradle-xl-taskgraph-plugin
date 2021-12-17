package com.xebialabs.gradle.plugins.taskgraph

import org.gradle.api.Task

class TaskNode {
    Task task
    boolean isUninteresting
    Set<Task> subtasks = new HashSet<Task>()
    private Set<Task> interestingSubtasks = null
    Set<Task> referencedByTasks = new HashSet<Task>()

    def getInterestingSubtasks(allTaskNodes) {
        if (interestingSubtasks == null) {
            interestingSubtasks = new HashSet<Task>()
            if (!isUninteresting) {
                interestingSubtasks.add(this.task);
            } else {
                subtasks.each { interestingSubtasks.addAll(allTaskNodes.get(it).getInterestingSubtasks(allTaskNodes)) }
            }
        }
        return interestingSubtasks
    }
}
