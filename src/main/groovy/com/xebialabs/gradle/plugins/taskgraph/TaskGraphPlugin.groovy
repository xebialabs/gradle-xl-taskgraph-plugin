package com.xebialabs.gradle.plugins.taskgraph

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class TaskGraphPlugin implements Plugin<Project> {

  private static final Logger log = LoggerFactory.getLogger(TaskGraphPlugin.class)

  private static Project root = null
  private static Date start;
  static def defaultUninterestingTasks = [
      "compileJava",
      "compileScala",
      "checkJavaVersion",
      "processResources",
      "classes"
  ]

  @Override
  void apply(Project project) {
    if (!project.hasProperty("taskgraph")) {
      log.info "taskgraph property not found on project " + project.path
      return
    }

    if (root != null) {
      return
    }

    root = project.rootProject

    project.gradle.taskGraph.whenReady {
      log.info "Generating task graph..."
      generateGraphTxt()
      convertToPng()
    }
  }

  static def generateGraphTxt() {
    start = new Date()
    def tasks = root.gradle.taskGraph.getAllTasks()
    def text = "digraph {\n rankdir = LR ; \n concentrate = true ; \n"
    def uninterestingTaskNames = root.hasProperty("fullgraph") ? [] : defaultUninterestingTasks
    def grayEdgeStyle = "[style = \"dotted\"]"

    def allTaskNodes = new HashMap<Task, TaskNode>()
    tasks.each {
      log.debug("Registering task " + it.name)
      def node = new TaskNode()
      node.task = it
      node.isUninteresting = (uninterestingTaskNames.contains(it.name))
      allTaskNodes.put(it, node)
    }

    tasks.each { task ->
      task.getTaskDependencies().getDependencies().each { deptask ->
        log.debug("Task " + task.path + " depends on " + deptask.path)
        def tasknode = allTaskNodes.get(task)
        def deptasknode = allTaskNodes.get(deptask)
        if (deptasknode != null) {
          tasknode.subtasks.add(deptask)
          deptasknode.referencedByTasks.add(task)
        } else {
          log.info("task ${task.path} depends on task ${deptask.path}, but it was excluded from the build")
        }
      }
    }

    allTaskNodes.each { task, node ->
      if (!node.isUninteresting) text += "\"${task.path}\" ; \n"
    }

    allTaskNodes.each { task, node ->
      if (!node.isUninteresting) {
        node.subtasks.each { deptask ->
          allTaskNodes.get(deptask).getInterestingSubtasks(allTaskNodes).each { ist ->
            def style = (allTaskNodes.get(deptask).isUninteresting) ? grayEdgeStyle : ""
            text += "\"${task.path}\" -> \"${ist.path}\" $style ; \n"
          }
        }

      }
    }
    text += "}"

    def outputTxtFile = new File(root.buildDir, "taskgraph.txt")
    log.info("Writing GraphViz graph data to " + outputTxtFile.canonicalPath)
    root.buildDir.mkdir()
    outputTxtFile.text = text
  }

  static def convertToPng() {
    if (root.hasProperty("dotExecutable")) {
      def outputTxtFile = new File(root.buildDir, "taskgraph.txt")
      log.info("Converting taskgraph using " + root.getProperty("dotExecutable"))
      def outputPngFile = new File(root.buildDir, "taskgraph.png")
      def outputStream = new FileOutputStream(outputPngFile)
      def errorStream = new ByteArrayOutputStream()
      root.exec {
        executable root.getProperty("dotExecutable")
        args "-Tpng", outputTxtFile.canonicalPath
        standardOutput outputStream
        errorOutput errorStream
      }
      def errors = errorStream.toString()
      if (errors == null || errors.isEmpty() || errors.isAllWhitespace()) {
        log.warn("Task graph should now be in " + outputPngFile.canonicalFile)
      } else {
        log.warn("Errors while generating graph: " + errors)
      }
    } else {
      log.info("property [dotExecutable] not set - not converting .txt->.png")
    }
    log.info("Task graph generation took (ms): " + (new Date().time - start.time))
  }
}
