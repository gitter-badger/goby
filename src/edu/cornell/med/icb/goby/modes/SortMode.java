/*
 * Copyright (C) 2009-2012 Institute for Computational Biomedicine,
 *                    Weill Medical College of Cornell University
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package edu.cornell.med.icb.goby.modes;

import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import edu.cornell.med.icb.goby.alignments.*;
import edu.cornell.med.icb.goby.util.HeaderUtil;
import edu.cornell.med.icb.util.ICBStringUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.logging.ProgressLogger;
import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;


/**
 * Sort an alignment by reference and reference position for very large alignments.
 * Splits the alignment into chunks, sorting each chunk, then successively merging
 * the chunks until the complete output is sorted.
 *
 * @author Kevin Dorff.
 * @author Fabien Campagne.
 */
public class SortMode extends AbstractGobyMode {
    /**
     * Used to log debug and informational messages.
     */
    private static final org.apache.log4j.Logger LOG = Logger.getLogger(SortMode.class);

    /**
     * The mode name.
     */
    private static final String MODE_NAME = "sort";

    /**
     * The mode description help text.
     */
    private static final String MODE_DESCRIPTION = "Sort a large (any size) compact alignment files by reference position. This sort mode works in parallel, splitting the alignment, sorting the splits, then successively merging the sorted pieces. The output alignment is sorted and indexed.";

    /**
     * The output file.
     */
    private String outputFilename;

    /**
     * The basename of the compact alignment.
     */
    private String basename;

    private int numThreads = -1;
    private int filesPerMerge = 30;
    private long splitSize = -1;
    private String tempDir = "/tmp";

    private double memoryPercentageForWork = 0.75;
    private int splitSizeScalingFactor = 100;

    /*
     * The following data structures are used to assist the parallelization of the sort/merge.
     */
    // Splits that are waiting to be sorted/merged
    private final ConcurrentLinkedQueue<SortMergeSplit> splitsToMerge = new ConcurrentLinkedQueue<SortMergeSplit>();
    // Number of splits that are waiting to be sorted/merged. Not just using splitsToMerge.size() for efficiency
    private final AtomicInteger splitsToMergeSize = new AtomicInteger(0);
    private final AtomicInteger numSplitsCompleted = new AtomicInteger(0);
    // Splits that have been sorted or sorted/merged, waiting for the next sort/merge
    private final ConcurrentLinkedQueue<SortMergeSplit> sortedSplits = new ConcurrentLinkedQueue<SortMergeSplit>();
    // The number of sorts or sorts/merges that are running or queued right now
    private final AtomicLong numSortMergesRunning = new AtomicLong(0);
    private final AtomicLong numMergesExecuted = new AtomicLong(0);
    private ExecutorService executorService;

    private final ConcurrentLinkedQueue<Throwable> exceptions = new ConcurrentLinkedQueue<Throwable>();
    private final ConcurrentLinkedQueue<File> filesToDelete = new ConcurrentLinkedQueue<File>();

    private ProgressLogger progressSplitSort;
    private ProgressLogger progressMergeSort;
    private boolean dryRun;

    @Override
    public String getModeName() {
        return MODE_NAME;
    }

    /*
    @Override
    public String getShortModeName() {
        return "s";
    }
    */


    @Override
    public String getModeDescription() {
        return MODE_DESCRIPTION;
    }


    public void setInput(final String input) {
        basename = AlignmentReaderImpl.getBasename(input);
    }

    public void setOutput(final String output) {
        outputFilename = output;
    }

    /**
     * Get the number of threads sort/merge will use.
     *
     * @return the number of threads to use
     */
    public int getNumThreads() {
        return numThreads;
    }

    /**
     * Set the number of threads to use.
     * Set to < 0 for auto-detect number of cores.
     * Set to 0 for single threaded, not using the thread pool
     * Set to >= 1 for multi threaded AND using the thread pool
     * (1 is still single threaded, but it uses the thread pool)
     *
     * @param numThreads the number of threads to use
     */
    public void setNumThreads(final int numThreads) {
        if (numThreads < 0) {
            this.numThreads = Runtime.getRuntime().availableProcessors();
        } else {
            this.numThreads = numThreads;
        }
    }

    /**
     * Get the number of files per merge.
     * The number of splits / files per merge. The maximum value you can use for this is related to the
     * number of file descriptors available from your OS. sort-large mode will open approximately
     * (3 * threads * files-per-merge) during the merge process.
     *
     * @return the number of files per merge.
     */
    public int getFilesPerMerge() {
        return filesPerMerge;
    }

    /**
     * Set the number of files per merge.
     * The number of splits / files per merge. The maximum value you can use for this is related to the
     * number of file descriptors available from your OS. sort-large mode will open approximately
     * ((4 * num-threads * files-per-merge) + 4) during the merge process.
     *
     * @param filesPerMerge the number of files per merge.
     */
    public void setFilesPerMerge(final int filesPerMerge) {
        this.filesPerMerge = filesPerMerge;
    }

    /**
     * Get the split size in bytes.
     * The size of the split in bytes. The default, -1, attempts to guess the split size based on
     * available memory including overhead. Keep in mind that threads you run with, the less
     * available memory to each parallel sort.
     *
     * @return the split size
     */
    public long getSplitSize() {
        return splitSize;
    }

    /**
     * Set the split size in bytes.
     * The size of the split in bytes. The default, -1, attempts to guess the split size based on
     * available memory including overhead. Keep in mind that threads you run with, the less
     * available memory to each parallel sort.
     *
     * @param splitSize the split size
     */
    public void setSplitSize(final long splitSize) {
        if (splitSize > 0) {
            this.splitSize = splitSize;
        } else {
            this.splitSize = -1;
        }
    }

    /**
     * Get splitSizeScalingFactor.
     * The larger the value of split-size-scaling-factor the more splits will be made because this
     * value controls the COMPRESSED size of a single split, based on the equation
     * ((TOTAL_AVAILABLE_MEMORY * memory-percentage-for-work) / (num-threads * split-size-scaling-factor))
     * This means the larger the split-size-scaling-factor, the smaller the the split-size will be
     * per thread. If, during sort-large-mode, you are running out of memory, INCREASE the value for
     * split-size-scaling-factor. Also, if you are running out of memory, it is advisable to increase the
     * overall memory available to Java using -Xmx and -Xms, such as "-Xmx8g -Xms8g" to run
     * the program with a total of 8g of memory. The relatively large value for this (the default is 100)
     * is required because the alignments will be decompressed after being read.
     *
     * @return the current splitSizeScalingFactor
     */
    public int getSplitSizeScalingFactor() {
        return splitSizeScalingFactor;
    }

    /**
     * Set splitSizeScalingFactor.
     * The larger the value of split-size-scaling-factor the more splits will be made because this
     * value controls the COMPRESSED size of a single split, based on the equation
     * ((TOTAL_AVAILABLE_MEMORY * memory-percentage-for-work) / (num-threads * split-size-scaling-factor))
     * This means the larger the split-size-scaling-factor, the smaller the the split-size will be
     * per thread. If, during sort-large-mode, you are running out of memory, INCREASE the value for
     * split-size-scaling-factor. Also, if you are running out of memory, it is advisable to increase the
     * overall memory available to Java using -Xmx and -Xms, such as "-Xmx8g -Xms8g" to run
     * the program with a total of 8g of memory. The relatively large value for this (the default is 100)
     * is required because the alignments will be decompressed after being read.
     *
     * @param splitSizeScalingFactor the current splitSizeScalingFactor
     */
    public void setSplitSizeScalingFactor(final int splitSizeScalingFactor) {
        this.splitSizeScalingFactor = splitSizeScalingFactor;
    }

    /**
     * Get memoryPercentageForWork.
     * The percentage of available memory available to worker threads
     * (to increase overall memory available to Java using -Xmx and -Xms, such as "-Xmx8g -Xms8g" to run
     * the program with a total of 8g of memory). Also see split-size-scaling-factor
     * documentation.
     *
     * @return the current memoryPercentageForWork value
     */
    public double getMemoryPercentageForWork() {
        return memoryPercentageForWork;
    }

    /**
     * Set memoryPercentageForWork.
     * The percentage of available memory available to worker threads
     * (to increase overall memory available to Java using -Xmx and -Xms, such as "-Xmx8g -Xms8g" to run
     * the program with a total of 8g of memory). Also see split-size-scaling-factor
     * documentation. You cannot set a value larger than "0.99" or less than "0.5".
     *
     * @param memoryPercentageForWork the new memoryPercentageForWork value
     */
    public void setMemoryPercentageForWork(final double memoryPercentageForWork) {
        if (memoryPercentageForWork > 0.99) {
            this.memoryPercentageForWork = 0.99;
        } else if (memoryPercentageForWork < 0.5) {
            this.memoryPercentageForWork = 0.5;
        } else {
            this.memoryPercentageForWork = memoryPercentageForWork;
        }
    }

    /**
     * Configure.
     *
     * @param args command line arguments
     * @return this object for chaining
     * @throws java.io.IOException error parsing
     * @throws com.martiansoftware.jsap.JSAPException
     *                             error parsing
     */
    @Override
    public AbstractCommandLineMode configure(final String[] args)
            throws IOException, JSAPException {
        final JSAPResult jsapResult = parseJsapArguments(args);

        setInput(jsapResult.getString("input"));
        outputFilename = jsapResult.getString("output");
        setNumThreads(jsapResult.getInt("num-threads"));
        filesPerMerge = jsapResult.getInt("files-per-merge");
        setSplitSize(jsapResult.getLong("split-size"));
        tempDir = jsapResult.getString("temp-dir");
        dryRun = jsapResult.getBoolean("dry-run");

        setMemoryPercentageForWork(jsapResult.getDouble("memory-percentage-for-work"));
        splitSizeScalingFactor = jsapResult.getInt("thread-memory-scaling-factor");

        return this;
    }

    /**
     * Sort the alignment.
     *
     * @throws java.io.IOException error reading / writing
     */
    @Override
    public void execute() throws IOException {
        final String threadId = String.format("%02d", Thread.currentThread().getId());
        if (splitSize <= 0) {
            final long allocatedHeapSize = Runtime.getRuntime().totalMemory();
            final long freeInHeap = Runtime.getRuntime().freeMemory();
            final long maxHeapSize = Runtime.getRuntime().maxMemory();
            final long freeMemory = maxHeapSize - allocatedHeapSize + freeInHeap;//Util.availableMemory();

            splitSize = (long) (freeMemory * memoryPercentageForWork) /
                    (long) ((numThreads > 0 ? numThreads : 1) * splitSizeScalingFactor);
            LOG.info(String.format("Maximum memory is %s. Using a split-size of %s",
                    ICBStringUtils.humanMemorySize(freeMemory), ICBStringUtils.humanMemorySize(splitSize)));
        }

        final File entriesFile = new File(basename + ".entries");
        if (!entriesFile.exists()) {
            System.err.println("Could not locate alignment .entries file " + entriesFile.toString());
            return;
        }

        final long fileSize = entriesFile.length();

        // Reduce the number of processors by one, as one thread is used by this running program
        // and it will be utilized since we've chosen CallerRunsPolicy
        LOG.debug(String.format("sort-large will run with %d threads (0 == no thread pool)", numThreads));
        final AlignmentReader reader = new AlignmentReaderImpl(basename);
        try {
            reader.readHeader();
            if (reader.isSorted()) {
                LOG.warn("Warning: The input alignment is already sorted.");
            }
        } finally {
            reader.close();
        }
        if (numThreads > 0) {
            executorService = new ThreadPoolExecutor(
                    numThreads, // core thread pool size
                    numThreads, // maximum thread pool size
                    10, // time to wait before resizing pool
                    TimeUnit.MINUTES,
                    new LinkedBlockingQueue<Runnable>());
            //new ArrayBlockingQueue<Runnable>(additionalThreads, true));
            //new ThreadPoolExecutor.CallerRunsPolicy()*/);
        }

        // Setup splits and start first pass sort
        LOG.debug("Splitting file and sorting all splits");
        long numberOfSplits = 0;
        long splitStart = 0;
        boolean lastSplit = false;
        boolean firstSort = true;

        progressSplitSort = new ProgressLogger(LOG, "split-sorts");
        progressSplitSort.displayFreeMemory = true;

        int count = 0;
        ObjectArrayList<Runnable> splits = new ObjectArrayList<Runnable>();
        while (!lastSplit) {
            long splitEnd = splitStart + splitSize;
            if (splitEnd >= fileSize - 1) {
                splitEnd = fileSize - 1;
                lastSplit = true;
            }
            final SortMergeSplit split = new SortMergeSplit(splitStart, splitEnd);
            numberOfSplits++;
            splits.add(sortSplit(split, firstSort));
            firstSort = false;
            splitStart = splitEnd;

        }
        LOG.info(String.format("[%s] Split file into %d pieces", threadId, numberOfSplits));

        progressSplitSort.expectedUpdates = numberOfSplits;
        progressSplitSort.start();
        for (Runnable toRun : splits) {
            if (executorService != null) {
                executorService.submit(toRun);
            } else {
                toRun.run();
            }
        }
        while (numSplitsCompleted.get() != numberOfSplits) {
            // Wait a bit for tasks to finish before finding more to submit
            if (!exceptions.isEmpty()) {
                break;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        progressSplitSort.done();

        progressMergeSort = new ProgressLogger(LOG, "merges");
        progressMergeSort.displayFreeMemory = true;
        progressMergeSort.expectedUpdates = numberOfSplits;
        progressMergeSort.start();
        // Subsequent sorts
        boolean lastMerge = false;
        boolean done = false;
        while (!done) {

            if (!exceptions.isEmpty()) {
                break;
            }

            // Move any completed sorts back into the splitsToMerge queue
            while (true) {
                final SortMergeSplit sortedSplit = sortedSplits.poll();
                if (sortedSplit != null) {
                    splitsToMerge.add(sortedSplit);
                    numSortMergesRunning.decrementAndGet();
                    splitsToMergeSize.incrementAndGet();
                } else {
                    break;
                }
            }
            int splitsToMergeSizeLocal = splitsToMergeSize.get();
            if (lastMerge && splitsToMergeSizeLocal == 1) {
                // We're done
                break;
            }

            final int numSplitsForMerge;
            if (splitsToMergeSizeLocal == numberOfSplits && numberOfSplits <= filesPerMerge) {
                numSplitsForMerge = (int) numberOfSplits;
                lastMerge = true;
            } else if (splitsToMergeSizeLocal == 0) {
                // Nothing to sort this iteration
                numSplitsForMerge = 0;
            } else if (splitsToMergeSizeLocal == 1) {
                // Just one thing to sort, but it's not then complete merge yet. Wait.
                numSplitsForMerge = 0;
            } else if (splitsToMergeSizeLocal > filesPerMerge) {
                numSplitsForMerge = filesPerMerge;
            } else {
                // Equal to or less than filesPerMerge. Perhaps the last merge?
                final List<SortMergeSplitFileRange> ranges = mergeMultiSplitRangeLists(splitsToMerge);
                if (ranges.size() == 1 && ranges.get(0).isRange(0, fileSize - 1)) {
                    // Last merge.
                    lastMerge = true;
                    numSplitsForMerge = splitsToMergeSizeLocal;
                } else if (splitsToMergeSizeLocal == filesPerMerge) {
                    // We have enough to merge, but it's not the last merge
                    numSplitsForMerge = splitsToMergeSizeLocal;
                } else {
                    // We don't have enough to merge and it's not the last merge
                    numSplitsForMerge = 0;
                }
            }


            if (numSplitsForMerge > 0) {
                final List<SortMergeSplit> toMerge = new ArrayList<SortMergeSplit>(numSplitsForMerge);
                for (int i = 0; i < numSplitsForMerge; i++) {
                    splitsToMergeSizeLocal = splitsToMergeSize.decrementAndGet();
                    toMerge.add(splitsToMerge.poll());
                }
                LOG.debug(String.format("[%s] %d items in queue to sort after removing %d for sorting",
                        threadId, splitsToMergeSizeLocal, numSplitsForMerge));
                mergeSplits(toMerge, lastMerge);

            } else {
                // Wait a bit for tasks to finish before finding more to submit
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        if (executorService != null) {
            LOG.debug(String.format("[%s] Waiting for threads to finish.", threadId));
            // accept no new tasks, but wait for all of the executor threads to finish :
            executorService.shutdown();
            try {
                while (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        progressMergeSort.stop();

        if (!filesToDelete.isEmpty()) {
            // These files weren't deleted after merge for some reason. We'll try again one more time.
            while (true) {
                final File cleanupFile = filesToDelete.poll();
                if (cleanupFile == null) {
                    break;
                } else {
                    deleteFile(cleanupFile, false);
                }
            }
        }

        if (exceptions.isEmpty()) {

            System.err.println("Sort completed");

            final SortMergeSplit fullFile = splitsToMerge.poll();
            LOG.info(String.format("%s made up from %d splits", fullFile, fullFile.numFiles));
            LOG.info(String.format("Took %d secondary sort/merges", numMergesExecuted.get()));
        } else {
            LOG.error("Potentially multiple exceptions follow");
            for (final Throwable t : exceptions) {
                LOG.error(t);
            }
        }

    }

    /**
     * Initial sort of one split
     *
     * @param toSort    the split to sort
     * @param firstSort if this is the first sort (TMH will be written only during the first sort)
     */
    private Runnable sortSplit(final SortMergeSplit toSort, final boolean firstSort) {
        numSortMergesRunning.incrementAndGet();
        final Runnable toRun = new Runnable() {
            @Override
            public void run() {
                if (dryRun) {
                    System.out.println("dry-run: will sort-split " + toSort);
                    sortedSplits.add(toSort);
                    numSplitsCompleted.incrementAndGet();
                    return;
                }
                // Before sort

                // SORT
                // Simulate sort time
                final String threadId = String.format("%02d", Thread.currentThread().getId());
                AlignmentWriterImpl writer = null;
                try {
                    System.gc();
                    LOG.debug(String.format("[%s] Sorting %s", threadId, toSort.toString()));

                    final SortIterateAlignments alignmentIterator = new SortIterateAlignments();
                    final String subBasename = "sorted-" + toSort.tag;
                    final String subOutputFilename = tempDir + "/" + subBasename;
                    LOG.debug(String.format("[%s] Sorting %s to %s",
                            threadId, toSort.toString(), subOutputFilename));
                    writer = new AlignmentWriterImpl(subOutputFilename);
                    alignmentIterator.setOutputFilename(subOutputFilename);
                    alignmentIterator.setBasename(subBasename);

                    // Iterate through each alignment and entries to output file:
                    LOG.debug(String.format("[%s] Loading entries...", threadId));
                    final SortMergeSplitFileRange range = toSort.ranges.get(0);
                    alignmentIterator.iterate(range.min, range.max, basename);
                    LOG.debug(String.format("[%s] Sorting...", threadId));
                    alignmentIterator.sort();
                    LOG.debug(String.format("[%s] Writing sorted alignment for %s with tag %s...", threadId, toSort.toString(), toSort.tag));
                    AlignmentReader alignmentReader = null;

                    try {

                        if (firstSort) {
                            LOG.debug(String.format("[%s] Writing TMH", threadId));
                            alignmentReader = new AlignmentReaderImpl(0, 0, basename, false);
                            alignmentReader.readHeader();
                            Merge.prepareMergedTooManyHits(outputFilename, alignmentReader.getNumberOfQueries(), 0, basename);
                            writer.setNumQueries(alignmentReader.getNumberOfQueries());
                        }
                        alignmentIterator.write(writer);


                    } finally {
                        // Sort finished
                        if (alignmentReader != null) {
                            alignmentReader.close();
                        }
                        deleteOnExit(threadId, subOutputFilename);
                        checkBasename(threadId, subOutputFilename);
                        sortedSplits.add(toSort);
                    }

                } catch (Throwable e) {
                    LOG.error(String.format("[%s] Exception sorting!! class=%s message=%s", threadId, e.getClass().getName(), e.getMessage()));
                    e.printStackTrace();
                    exceptions.add(e);
                } finally {
                    progressSplitSort.update();
                    numSplitsCompleted.incrementAndGet();
                }
            }
        };
        return toRun;
    }

    private void deleteOnExit(String threadId, String basename) {

        new File(basename + ".entries").deleteOnExit();
        new File(basename + ".header").deleteOnExit();
        new File(basename + ".index").deleteOnExit();
        new File(basename + ".stats").deleteOnExit();

    }

    /**
     * Subsequent merge of multiple splits
     *
     * @param toMerge   the splits to sort
     * @param lastMerge if this is the very last merge
     */
    private void mergeSplits(final List<SortMergeSplit> toMerge, final boolean lastMerge) {
        numSortMergesRunning.incrementAndGet();
        final Runnable toRun = new Runnable() {
            @Override
            public void run() {

                final String threadId = String.format("%02d", Thread.currentThread().getId());
                final List<String> mergeFromBasenames = new LinkedList<String>();
                ConcatSortedAlignmentReader concatReader = null;
                AlignmentWriter writer = null;
                try {
                    System.gc();
                    // Prepare to merge
                    final SortMergeSplit merged = new SortMergeSplit(toMerge.get(0));

                    final int numSplits = toMerge.size();
                    for (int i = 1; i < numSplits; i++) {
                        merged.mergeWith(toMerge.get(i));
                    }
                    merged.ranges = mergeRangeList(merged.ranges);

                    LOG.debug(String.format("[%s] Merging %d items %s to %s",
                            threadId, numSplits,
                            ArrayUtils.toString(toMerge), ArrayUtils.toString(merged)));


                    for (final SortMergeSplit mergeFrom : toMerge) {
                        final String inputBasename = tempDir + "/sorted-" + mergeFrom.tag;
                        mergeFromBasenames.add(inputBasename);
                    }
                    // note that we don't adjust query indices because they are already not overlapping (all come from the
                    // same input file)
                    final String[] mergeFromBasenamesArray =
                            mergeFromBasenames.toArray(new String[mergeFromBasenames.size()]);
                    for (final String mergeBasename : mergeFromBasenamesArray) {
                        checkBasename(threadId, mergeBasename);
                    }
                    if (!dryRun) {
                        concatReader = new ConcatSortedAlignmentReader(         false, mergeFromBasenamesArray);
                        concatReader.readHeader();
                    }
                    // We've used merged's tag as input. Let's get a new tag for it's output
                    merged.makeNewTag();

                    final String subOutputFilename;
                    if (lastMerge) {
                        subOutputFilename = outputFilename;
                    } else {
                        final String subBasename = "sorted-" + merged.tag;
                        subOutputFilename = tempDir + "/" + subBasename;
                    }
                    if (!dryRun) {
                        writer = new AlignmentWriterImpl(subOutputFilename);
                        HeaderUtil.copyHeader(concatReader,writer);
                        writer.setSorted(true);

                        for (final Alignments.AlignmentEntry entry : concatReader) {
                            writer.appendEntry(entry);
                        }
                    }
                    if (dryRun) {
                        System.out.printf("dry-run: will sort-merge %d splits into %s %n", toMerge.size(), merged.toString());
                    }
                    // Sort/merge finished
                    numMergesExecuted.incrementAndGet();
                    sortedSplits.add(merged);
                } catch (Throwable e) {
                    LOG.error(String.format("[%s] Exception sorting!! class=%s message=%s", threadId, e.getClass().getName(), e.getMessage()));
                    e.printStackTrace();
                    exceptions.add(e);
                } finally {
                    try {
                        if (concatReader != null) {
                            concatReader.close();
                        }
                    } catch (IOException e) {
                        // Close quietly.
                        LOG.info("Exception closing concatReader", e);
                    }
                    try {
                        if (writer != null) {
                            writer.close();
                        }
                    } catch (IOException e) {
                        // Close quietly.
                        LOG.info("Exception closing writer", e);
                    }

                    // Delete the merged FROM files
                    for (final String mergeFromBasename : mergeFromBasenames) {
                        deleteFile(new File(mergeFromBasename + ".entries"), true);
                        deleteFile(new File(mergeFromBasename + ".header"), true);
                        deleteFile(new File(mergeFromBasename + ".index"), true);
                        deleteFile(new File(mergeFromBasename + ".stats"), true);
                    }

                    progressMergeSort.update(toMerge.size() - 1);
                }
            }
        };
        if (executorService != null) {
            executorService.submit(toRun);
        } else {
            toRun.run();
        }
    }

    private boolean checkBasename(final String threadId, final String basename) {
        boolean exists = true;
        exists &= checkFile(threadId, basename + ".entries");
        exists &= checkFile(threadId, basename + ".header");
        exists &= checkFile(threadId, basename + ".index");
        exists &= checkFile(threadId, basename + ".stats");
        return exists;
    }

    private boolean checkFile(final String threadId, final String filename) {
        final File file = new File(filename);
        final boolean exists = file.exists();
        //   LOG.debug(String.format("[%s] %s exists? %s", threadId, filename, exists ? "Yes" : "No"));
        return exists;
    }

    private void deleteFile(final File file, final boolean queueIfFailed) {
        if (!file.delete() && queueIfFailed) {
            filesToDelete.add(file);
        }
    }

    /**
     * Main method.
     *
     * @param args command line args.
     * @throws com.martiansoftware.jsap.JSAPException
     *                             error parsing
     * @throws java.io.IOException error parsing or executing.
     */

    public static void main(final String[] args) throws JSAPException, IOException {
        new SortMode().configure(args).execute();
    }

    public static List<SortMergeSplitFileRange> mergeMultiSplitRangeLists(final Collection<SortMergeSplit> splits) {
        final List<SortMergeSplitFileRange> ranges = new LinkedList<SortMergeSplitFileRange>();
        for (final SortMergeSplit split : splits) {
            ranges.addAll(split.ranges);
        }
        return mergeRangeList(ranges);
    }

    public static List<SortMergeSplitFileRange> mergeRangeList(final List<SortMergeSplitFileRange> ranges) {
        Collections.sort(ranges);
        final List<SortMergeSplitFileRange> result = new ArrayList<SortMergeSplitFileRange>(ranges.size());
        SortMergeSplitFileRange current = null;
        for (final SortMergeSplitFileRange range : ranges) {
            if (current == null) {
                current = range;
                result.add(current);
                continue;
            }
            if (current.max + 1 >= range.min) {
                current.max = range.max;
            } else {
                current = range;
                result.add(current);
            }
        }
        return result;
    }

    private class SortMergeSplit {
        List<SortMergeSplitFileRange> ranges;
        String tag;
        String prevTag;
        int numFiles;
        IOException exception;

        private SortMergeSplit(final long min, final long max) {
            final SortMergeSplitFileRange range = new SortMergeSplitFileRange(min, max);
            ranges = new ArrayList<SortMergeSplitFileRange>();
            ranges.add(range);
            numFiles = 1;
            tag = ICBStringUtils.generateRandomString(10);
        }

        public SortMergeSplit(SortMergeSplit source) {
            ranges = new ArrayList<SortMergeSplitFileRange>();
            ranges.addAll(source.ranges);
            numFiles = 1;
            tag = ICBStringUtils.generateRandomString(10);
        }

        private long min() {
            long min = Integer.MAX_VALUE;
            for (SortMergeSplitFileRange range : ranges) {
                min = Math.min(min, range.min);
            }
            return min;
        }

        private long max() {
            long max = Integer.MIN_VALUE;
            for (SortMergeSplitFileRange range : ranges) {
                max = Math.max(max, range.max);
            }
            return max;
        }

        /**
         * Add a split to this split (for merging)
         *
         * @param split the split we are merging this one with
         */
        private void addRangesFromSplit(final SortMergeSplit split) {
            ranges.addAll(split.ranges);
        }

        /**
         * Save the previous tag, make a new one.
         */
        private void makeNewTag() {
            prevTag = tag;
            tag = ICBStringUtils.generateRandomString(10);
        }

        public String toString() {
            return ArrayUtils.toString(ranges);
        }

        /**
         * Add the content of source to this range.
         *
         * @param sortMergeSplit
         */
        public void mergeWith(SortMergeSplit sortMergeSplit) {
            addRangesFromSplit(sortMergeSplit);
            numFiles += sortMergeSplit.numFiles;
        }
    }

    private class SortMergeSplitFileRange implements Comparable<SortMergeSplitFileRange> {
        long min;
        long max;

        private SortMergeSplitFileRange(final long min, final long max) {
            this.min = min;
            this.max = max;
        }

        public boolean isRange(final long isMin, final long isMax) {
            return min == isMin && max == isMax;
        }

        public int compareTo(final SortMergeSplitFileRange other) {
            if (this.min < other.min) {
                return -1;
            } else if (this.min == other.min) {
                return 0;
            } else {
                return 1;
            }
        }

        public String toString() {
            return "[" + min + "," + max + "]";
        }
    }
}
