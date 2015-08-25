package io.breen.socrates.immutable.hooks;

import io.breen.socrates.immutable.criteria.Resource;
import io.breen.socrates.immutable.file.File;
import io.breen.socrates.immutable.hooks.triggers.FileHook;
import io.breen.socrates.immutable.hooks.triggers.Hook;
import io.breen.socrates.immutable.hooks.triggers.TestHook;
import io.breen.socrates.immutable.test.Test;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Static singleton class managing all hooks-related responsibility, such as registering
 * tasks to hooks or running hooks.
 */
public final class HookManager {

    private static Logger logger = Logger.getLogger(HookManager.class.getName());
    private static final Map<Hook, List<HookTask>> tasks;
    private static final Map<FileHook, Map<File, List<HookTask>>> fileTasks;
    private static final Map<TestHook, Map<Test, List<HookTask>>> testTasks;

    static {
        tasks = new HashMap<>();
        fileTasks = new HashMap<>();
        testTasks = new HashMap<>();

        for (Hook t : Hook.values())
            tasks.put(t, new LinkedList<>());

        for (FileHook t : FileHook.values())
            fileTasks.put(t, new HashMap<>());

        for (TestHook t : TestHook.values())
            testTasks.put(t, new HashMap<>());
    }

    private HookManager() {}

    public static void register(Hook hook, Resource script) {
        tasks.get(hook).add(new HookTask(script));
    }

    public static void registerFileHook(FileHook hook, Resource script, File file) {
        Map<File, List<HookTask>> mapForTrigger = fileTasks.get(hook);

        logger.info("registering " + script + " for " + file + " on " + hook);
        if (mapForTrigger.containsKey(file)) {
            mapForTrigger.get(file).add(new HookTask(script));
        } else {
            LinkedList<HookTask> tasksList = new LinkedList<>();
            tasksList.add(new HookTask(script));
            mapForTrigger.put(file, tasksList);
        }
    }

    public static void registerTestHook(TestHook hook, Resource script, Test test) {
        Map<Test, List<HookTask>> mapForTrigger = testTasks.get(hook);

        logger.info("registering " + script + " for " + test + " on " + hook);
        if (mapForTrigger.containsKey(test)) {
            mapForTrigger.get(test).add(new HookTask(script));
        } else {
            LinkedList<HookTask> tasksList = new LinkedList<>();
            tasksList.add(new HookTask(script));
            mapForTrigger.put(test, tasksList);
        }
    }

    public static void runHook(Hook hook) {
        List<HookTask> tasksForHook = tasks.get(hook);

        if (tasksForHook.isEmpty())
            return;

        logger.info("running hook " + hook);
        tasksForHook.forEach(HookTask::run);
    }

    public static void runHookForFile(FileHook hook, File file) {
        Map<File, List<HookTask>> mapForHook = fileTasks.get(hook);

        if (!mapForHook.containsKey(file))
            return;

        List<HookTask> tasksForFile = mapForHook.get(file);
        logger.info("running hook " + hook + " for file " + file);
        tasksForFile.forEach(HookTask::run);
    }

    public static void runHookForTest(TestHook hook, Test test) {
        Map<Test, List<HookTask>> mapForHook = testTasks.get(hook);

        if (!mapForHook.containsKey(test))
            return;

        List<HookTask> tasksForTest = mapForHook.get(test);
        logger.info("running hook " + hook + " for test " + test);
        tasksForTest.forEach(HookTask::run);
    }
}
