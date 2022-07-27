package os.hw1;

public class RunningProcess {
    private final Process process;
    private ProcessHandle master;
    private ProcessHandle cache;
    private final ProcessHandle[] workers;

    public RunningProcess(Process process, int workerCount) {
        this.process = process;
        this.workers = new ProcessHandle[workerCount];
    }

    public Process getProcess() {
        return process;
    }

    public ProcessHandle getMaster() {
        return master;
    }

    public void setMaster(ProcessHandle master) {
        this.master = master;
    }

    public ProcessHandle getCache() {
        return cache;
    }

    public void setCache(ProcessHandle cache) {
        this.cache = cache;
    }

    public ProcessHandle[] getWorkers() {
        return workers;
    }

    public void setWorker(int workerNum, ProcessHandle worker) {
        this.workers[workerNum] = worker;
    }
}
