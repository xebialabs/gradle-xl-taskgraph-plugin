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

Optional, but very handy: install `dot` from the GraphViz package, and add a line to your `gradle.properties`:

    dotExecutable=/path/to/graphviz/dot
    
Then, run your gradle task with the `-Ptaskgraph` switch:

    ./gradlew build -Ptaskgraph

After gradle's configuration stage, there will be a new file `<rootProject>/build/taskgraph.png` containing a visual representation of the task graph. (The corresponding `.txt` file contains its source.)

# --no-daemon
 
The daemon doesn't play nice here and may cause taskgraph generation to be skipped. In that case the gradle option `--no-daemon` is your friend. If you don't want to re-run the full task, combine it with `-m` which effectively skips all tasks (dry-run) leaving only task configuration and taskgraph generation. So, e.g.:

    ./gradlew build -Ptaskgraph --no-daemon -m
    

# Omitted tasks

Tasks with these names are, by default, omitted from the graph:
 
* `compileJava`
* `compileScala`
* `checkJavaVersion`
* `processResources`
* `classes`

When task A depends transitively on some other task B via a task in this list, a dotted line is used.
 
If you want to include these 'uninteresting' tasks in the graph anyway, add the gradle switch `-Pfullgraph`, e.g.:

    ./gradlew build -Ptaskgraph -Pfullgraph
    
