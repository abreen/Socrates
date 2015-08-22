package io.breen.socrates.immutable.hooks;

import io.breen.socrates.Globals;
import io.breen.socrates.immutable.criteria.Resource;
import io.breen.socrates.python.PythonProcessBuilder;

import java.nio.file.Path;
import java.util.logging.Logger;

/**
 *
 */
class HookTask implements Runnable {

    private static Logger logger = Logger.getLogger(HookTask.class.getName());
    public final Resource script;

    public HookTask(Resource script) {
        this.script = script;
    }

    @Override
    public void run() {
        logger.info("running HookTask: " + this);

        try {
            Path path = script.getPath();
            PythonProcessBuilder builder = new PythonProcessBuilder(path);
            Process process = builder.start();
            int exitCode = process.waitFor();
            if (exitCode != Globals.NORMAL_EXIT_CODE)
                throw new HookTaskAbnormalExit(this, exitCode);

        } catch (Exception e) {
            logger.severe("running HookTask " + this + " failed: " + e);
            throw new HookTaskRuntimeException(e);
        }
    }
}
