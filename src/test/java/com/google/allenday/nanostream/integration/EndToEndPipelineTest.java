package com.google.allenday.nanostream.integration;

import com.google.allenday.nanostream.gene.GeneBlobUriData;
import com.google.allenday.nanostream.gene.GeneData;
import com.google.allenday.nanostream.gene.GeneExampleMetaData;
import com.google.allenday.nanostream.gene.GeneRawData;
import com.google.allenday.nanostream.transform.AlignSortMergeTransform;
import org.apache.beam.runners.dataflow.DataflowRunner;
import org.apache.beam.runners.dataflow.options.DataflowPipelineOptions;
import org.apache.beam.runners.direct.DirectRunner;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.PipelineResult;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.testing.TestPipeline;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.GroupByKey;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.SimpleFunction;
import org.apache.beam.sdk.values.KV;
import org.junit.Rule;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.TimeZone;

/**
 * Tests full pipeline lifecycle in DataflowRunner mode
 */
public class EndToEndPipelineTest {

    private final static String SRC_BUCKET = "cannabis-3k";
    private final static String TEST_EXAMPLE_PROJECT = "SRP092005";
    private final static String TEST_EXAMPLE_SRA = "SRS1760342";
    private final static String TEST_EXAMPLE_RUN = "SRR4451179";
    private final static String RESULT_BUCKET = "cannabis-3k-results";
    private final static String REFERENCE_DIR = "reference/";

    @Rule
    public final transient TestPipeline testPipeline = TestPipeline.create();

    @Test
    public void testEndToEndPipeline() {
        DataflowPipelineOptions pipelineOptions = PipelineOptionsFactory
                .as(DataflowPipelineOptions.class);
        pipelineOptions.setRunner(DataflowRunner.class);
//        pipelineOptions.setRunner(DirectRunner.class);

        pipelineOptions.setWorkerMachineType("n1-standard-8");
        pipelineOptions.setProject("cannabis-3k");
        pipelineOptions.setNumberOfWorkerHarnessThreads(1);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd--HH-mm-ss-z");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        String jobTime = simpleDateFormat.format(new Date());

        pipelineOptions.setJobName(String.format("nanostream-core-end-to-end-test-%s", jobTime));
        Pipeline pipeline = Pipeline.create(pipelineOptions);

        GeneExampleMetaData testGeneExampleMetaData =
                new GeneExampleMetaData("TestProject", TEST_EXAMPLE_PROJECT, "TestBioSample", TEST_EXAMPLE_SRA, TEST_EXAMPLE_RUN);

        String testFile1 = TEST_EXAMPLE_RUN + "_1.fastq";
        String testFile2 = TEST_EXAMPLE_RUN + "_2.fastq";

        pipeline
                .apply(Create.<KV<GeneExampleMetaData, GeneData>>of(
                        KV.of(testGeneExampleMetaData,
                                new GeneBlobUriData(testFile1,
                                        String.format("gs://%s/sra/%s/%s/%s", SRC_BUCKET, TEST_EXAMPLE_PROJECT, TEST_EXAMPLE_SRA, testFile1))),
                        KV.of(testGeneExampleMetaData,
                                new GeneBlobUriData(testFile2,
                                        String.format("gs://%s/sra/%s/%s/%s", SRC_BUCKET, TEST_EXAMPLE_PROJECT, TEST_EXAMPLE_SRA, testFile2)))

                ))
                .apply(new AlignSortMergeTransform("AlignSortMergeTransform",
                        SRC_BUCKET,
                        RESULT_BUCKET,
                        REFERENCE_DIR,
                        Collections.singletonList("AGQN03"),
                        String.format("testing/cannabis_processing_output/%s/result_aligned_bam/", jobTime),
                        String.format("testing/cannabis_processing_output/%s/result_sorted_bam/", jobTime),
                        String.format("testing/cannabis_processing_output/%s/result_merged_bam/", jobTime),
                        1024 * 2));

        PipelineResult pipelineResult = pipeline.run();
        pipelineResult.waitUntilFinish();
    }
}