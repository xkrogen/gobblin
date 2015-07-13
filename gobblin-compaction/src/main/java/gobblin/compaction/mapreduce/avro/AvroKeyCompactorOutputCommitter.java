package gobblin.compaction.mapreduce.avro;

import java.io.IOException;
import java.lang.reflect.Method;

import org.apache.commons.io.FilenameUtils;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.StatusReporter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.output.FileOutputCommitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Class used with {@link MRCompactorAvroKeyDedupJobRunner} to rename files as they
 * are being committed. In addition to moving files from their working directory to
 * the commit output directory, the files are named to include a timestamp and a
 * count of how many records the file contains, in the format
 * {recordCount}.{timestamp}.avro.
 */
public class AvroKeyCompactorOutputCommitter extends FileOutputCommitter {

  private static final Logger LOG = LoggerFactory.getLogger(AvroKeyCompactorOutputCommitter.class);

  public AvroKeyCompactorOutputCommitter(Path output, TaskAttemptContext context)
      throws IOException {
    super(output, context);
  }

  /**
   * Commits the task, moving files to their final committed location by delegating to
   * {@link FileOutputCommitter} to perform the actual moving. First, renames the
   * files to include the count of records contained within the file and a timestamp,
   * in the form {recordCount}.{timestamp}.avro. Then, the files are moved to their
   * committed location.
   */
  @Override
  public void commitTask(TaskAttemptContext context) throws IOException {

    Path taskAttemptPath = super.getTaskAttemptPath(context);
    FileSystem fs = taskAttemptPath.getFileSystem(context.getConfiguration());

    if (fs.exists(taskAttemptPath)) {
      long recordCount = getRecordCountCounter(context);
      String fileName = recordCount + "." + System.currentTimeMillis() + ".avro";

      for (FileStatus status : fs.listStatus(taskAttemptPath, new PathFilter() {
        @Override
        public boolean accept(Path path) {
          return FilenameUtils.isExtension(path.getName(), "avro");
        }
      })) {
        Path newPath = new Path(status.getPath().getParent(), fileName);
        LOG.info(String.format("Renaming %s to %s", status.getPath(), newPath));
        fs.rename(status.getPath(), newPath);
      }
    }

    super.commitTask(context, taskAttemptPath);
  }

  private long getRecordCountCounter(TaskAttemptContext context) {
    try {
      //In Hadoop 2, TaskAttemptContext.getCounter() is available
      Method getCounterMethod = context.getClass().getMethod("getCounter", Enum.class);
      return ((Counter) getCounterMethod.invoke(context, AvroKeyDedupReducer.EVENT_COUNTER.RECORD_COUNT)).getValue();
    } catch (NoSuchMethodException e) {
      //In Hadoop 1, TaskAttemptContext.getCounter() is not available
      //Have to cast context to TaskAttemptContext in the mapred package, then get a StatusReporter instance
      org.apache.hadoop.mapred.TaskAttemptContext mapredContext = (org.apache.hadoop.mapred.TaskAttemptContext) context;
      return ((StatusReporter) mapredContext.getProgressible())
          .getCounter(AvroKeyDedupReducer.EVENT_COUNTER.RECORD_COUNT).getValue();
    } catch (Exception e) {
      throw new RuntimeException("Error reading record count counter", e);
    }
  }
}
