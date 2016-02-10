package org.arquillian.spacelift.task

import groovy.transform.CompileStatic
import org.arquillian.spacelift.Spacelift
import org.arquillian.spacelift.gradle.BaseContainerizableObject
import org.arquillian.spacelift.gradle.DeferredValue
import org.arquillian.spacelift.gradle.GradleSpaceliftDelegate
import org.arquillian.spacelift.gradle.GradleTask
import org.arquillian.spacelift.process.CommandBuilder
import org.arquillian.spacelift.process.ProcessInteraction
import org.arquillian.spacelift.task.os.CommandTool

@CompileStatic
class DefaultGradleTask extends BaseContainerizableObject<DefaultGradleTask> implements GradleTask {

    DeferredValue<Object> command = DeferredValue.of(Object.class)

    DeferredValue<List> allowedExitCodes = DeferredValue.of(List.class).from([])

    DeferredValue<File> workingDirectory = DeferredValue.of(File.class).from(CommandTool.CURRENT_USER_DIR)

    DeferredValue<ProcessInteraction> interaction = DeferredValue.of(ProcessInteraction.class).from(GradleSpaceliftDelegate.ECHO_OUTPUT)

    DeferredValue<Boolean> isDaemon = DeferredValue.of(Boolean.class).from(false)

    DeferredValue<Map> environment = DeferredValue.of(Map.class).from([:])

    DefaultGradleTask(String name, Object parent) {
        super(name, parent)
    }

    DefaultGradleTask(String name, DefaultGradleTask other) {
        super(name, other)

        this.command = other.@command.copy()
        this.allowedExitCodes = other.@allowedExitCodes.copy()
        this.workingDirectory = other.@workingDirectory.copy()
        this.interaction = other.@interaction.copy()
        this.isDaemon = other.@isDaemon.copy()
        this.environment = other.@environment.copy()
    }

    @Override
    def DefaultGradleTask clone(String name) {
        new DefaultGradleTask(name, this)
    }

    @Override
    def TaskFactory factory() {

        final DefaultGradleTask parent = this

        new TaskFactory() {
            Task create() {
                Task task = (Task) new CommandTool() {
                    {
                        this.allowedExitCodes = allowedExitCodes.resolveWith(parent)
                        this.workingDirectory = workingDirectory.resolveWith(parent)
                        this.interaction = interaction.resolveWith(parent)
                        this.isDaemon = isDaemon.resolveWith(parent)
                        // FIXME transform to Map<String,String>, this should be handled by Spacelift
                        this.environment = environment.resolveWith(parent).collectEntries(new HashMap<String, String>()) { key, value ->
                            [(key.toString()): value.toString()]
                        }

                        Object commandBuilder = command.resolveWith(parent)

                        if (commandBuilder instanceof CommandBuilder) {
                            this.commandBuilder = (CommandBuilder) commandBuilder
                        } else if (commandBuilder instanceof CharSequence || commandBuilder instanceof CharSequence[]) {
                            this.commandBuilder = new CommandBuilder(commandBuilder)
                        } else if (commandBuilder instanceof List) {
                            this.commandBuilder = new CommandBuilder(commandBuilder as CharSequence[])
                        } else {
                            throw new IllegalArgumentException("Invalid definition of command in DSL, should be String, String[] or CommandBuilder but was ${commandBuilder}")
                        }
                    }

                    @Override
                    def String toString() {
                        "${name}:" + commandBuilder.toString()
                    }
                }

                task.setExecutionService(Spacelift.service())
                task
            }

            @Override
            Collection aliases() {
                [name]
            }
        }
    }

    @Override
    def String toString() {
        "GradleTask ${name}"
    }
}
