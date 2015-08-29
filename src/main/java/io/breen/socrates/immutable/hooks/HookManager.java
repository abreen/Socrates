package io.breen.socrates.immutable.hooks;

import io.breen.socrates.Globals;
import io.breen.socrates.immutable.criteria.Resource;
import io.breen.socrates.immutable.file.File;
import io.breen.socrates.immutable.hooks.triggers.*;
import io.breen.socrates.immutable.test.Test;
import io.breen.socrates.python.PythonProcessBuilder;

import java.nio.file.Path;
import java.util.*;
import java.util.logging.Logger;

/**
 * Static singleton class managing all hooks-related responsibility, such as registering tasks to
 * hooks or running hooks.
 */
public final class HookManager {

    private static final Map<Hook, List<Resource>> tasks;
    private static final Map<FileHook, Map<File, List<Resource>>> fileTasks;
    private static final Map<TestHook, Map<Test, List<Resource>>> testTasks;
    private static Logger logger = Logger.getLogger(HookManager.class.getName());

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
        tasks.get(hook).add(script);
    }

    public static void registerFileHook(FileHook hook, Resource script, File file) {
        Map<File, List<Resource>> mapForTrigger = fileTasks.get(hook);

        logger.info("registering " + script + " for " + file + " on " + hook);
        if (mapForTrigger.containsKey(file)) {
            mapForTrigger.get(file).add(script);
        } else {
            LinkedList<Resource> tasksList = new LinkedList<>();
            tasksList.add(script);
            mapForTrigger.put(file, tasksList);
        }
    }

    public static void registerTestHook(TestHook hook, Resource script, Test test) {
        Map<Test, List<Resource>> mapForTrigger = testTasks.get(hook);

        logger.info("registering " + script + " for " + test + " on " + hook);
        if (mapForTrigger.containsKey(test)) {
            mapForTrigger.get(test).add(script);
        } else {
            LinkedList<Resource> tasksList = new LinkedList<>();
            tasksList.add(script);
            mapForTrigger.put(test, tasksList);
        }
    }

    public static void runHook(Hook hook) {
        List<Resource> tasksForHook = tasks.get(hook);

        if (tasksForHook.isEmpty()) return;

        logger.info("running hook " + hook);
        tasksForHook.forEach(h -> run(h, null));
    }

    public static void runHookForFile(FileHook hook, File file, Path directory) {
        Map<File, List<Resource>> mapForHook = fileTasks.get(hook);

        if (!mapForHook.containsKey(file)) return;

        List<Resource> tasksForFile = mapForHook.get(file);
        logger.info("running hook " + hook + " for file " + file);
        tasksForFile.forEach(h -> run(h, directory));
    }

    public static void runHookForTest(TestHook hook, Test test, Path directory) {
        Map<Test, List<Resource>> mapForHook = testTasks.get(hook);

        if (!mapForHook.containsKey(test)) return;

        List<Resource> tasksForTest = mapForHook.get(test);
        logger.info("running hook " + hook + " for test " + test);
        tasksForTest.forEach(h -> run(h, directory));
    }

    private static void run(Resource script, Path workingDir) {
        logger.info("running script: " + script);

        try {
            Path path = script.getPath();
            PythonProcessBuilder builder = new PythonProcessBuilder(path);
            if (workingDir != null) builder.setDirectory(workingDir);

            Process process = builder.start();
            int exitCode = process.waitFor();
            if (exitCode != Globals.NORMAL_EXIT_CODE) throw new HookAbnormalExit(script, exitCode);

        } catch (Exception e) {
            logger.severe("running script " + script + " failed: " + e);
            throw new HookRuntimeException(e);
        }
    }
}
