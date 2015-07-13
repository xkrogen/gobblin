package gobblin.compaction.mapreduce.avro;

import java.io.IOException;

import org.apache.avro.mapreduce.AvroKeyOutputFormat;
import org.apache.hadoop.mapreduce.OutputCommitter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.output.FileOutputCommitter;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

/**
 * Class used with {@link MRCompactorAvroKeyDedupJobRunner} as an entirely normal
 * {@link AvroKeyOutputFormat}, except that the outputted file names contain
 * a timestamp and a count of how many records the file contains in the form:
 * {recordCount}.{timestamp}.avro
 */
public class AvroKeyCompactorOutputFormat<T> extends AvroKeyOutputFormat<T> {

  private FileOutputCommitter committer = null;

  @Override
  public synchronized OutputCommitter getOutputCommitter(TaskAttemptContext context) throws IOException {
    if (this.committer == null) {
      this.committer = new AvroKeyCompactorOutputCommitter(FileOutputFormat.getOutputPath(context), context);
    }
    return this.committer;
  }

}
