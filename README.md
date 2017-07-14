# gradle-xl-taskgraph-plugin
Task graph plugin

# Usage
Include this in your root project's build.gradle:

    buildscript {
        dependencies {
            classpath 'com.xebialabs.gradle.plugins:gradle-xl-taskgraph-plugin:<version>
        }
    }
    apply plugin: "xebialabs.taskgraph"

The plugin is installed in XebiaLabs' Nexus, so make sure your gradle knows about this repository, e.g. by applying the xebialabs.opinions plugin

Optional, but very handy: install `dot` from the GraphViz package, and add a line to your `gradle.properties`:

    dotExecutable=/path/to/graphviz/dot
    
Then, run your gradle task with the `-Ptaskgraph` switch:

    ./gradlew build -Ptaskgraph

After gradle's configuration stage, there will be a new file `<rootProject>/build/taskgraph.png` containing a visual representation of the task graph. (The corresponding `.txt` file contains its source.)

# -m (dry-run)
 
If you don't want to (re-)execute a gradle task, but you do want to see what would be triggered, use gradle's `-m` switch which effectively skips all tasks (dry-run) leaving only task configuration and taskgraph generation - the task graph will still be generated So, e.g.:

    ./gradlew build -Ptaskgraph -m

will give you the task graph in 3 seconds on XL Deploy (as of july 2017)

# Omitted tasks

Tasks with these names are, by default, omitted from the graph:
 
* `compileJava`
* `compileScala`
* `checkJavaVersion`
* `processResources`
* `classes`
* `testClasses`
* `compileTestJava`
* `compileTestScala`

When task A depends transitively on some other task B via a task in this list, a dotted line is used.
 
If you want to include these 'uninteresting' tasks in the graph anyway, add the gradle switch `-Pfullgraph`, e.g.:

    ./gradlew build -Ptaskgraph -Pfullgraph
    
