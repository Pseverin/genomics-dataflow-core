package com.google.allenday.nanostream.transform;

import com.google.allenday.nanostream.cmd.CmdExecutor;
import com.google.allenday.nanostream.cmd.WorkerSetupService;
import com.google.allenday.nanostream.gene.GeneData;
import com.google.allenday.nanostream.gene.GeneExampleMetaData;
import com.google.allenday.nanostream.gene.GeneReadGroupMetaData;
import com.google.allenday.nanostream.io.IoHandler;
import com.google.allenday.nanostream.merge.BamFilesMerger;
import org.apache.beam.sdk.transforms.*;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;

import javax.annotation.Nullable;
import java.util.List;

public class AlignSortMergeTransform extends PTransform<PCollection<KV<GeneExampleMetaData, GeneData>>, PCollection<KV<GeneReadGroupMetaData, GeneData>>> {

    private String srcBucket;
    private String destBucket;
    private String referenceDir;
    private List<String> referenceList;

    private String alignDestGcsPrefix;
    private String sortDestGcsPrefix;
    private String mergeDestGcsPrefix;
    private long memoryOutputLimit;

    public AlignSortMergeTransform(@Nullable String name, String srcBucket, String destBucket, String referenceDir,
                                   List<String> referenceList, String alignDestGcsPrefix, String sortDestGcsPrefix,
                                   String mergeDestGcsPrefix, long memoryOutputLimit) {
        super(name);
        this.srcBucket = srcBucket;
        this.destBucket = destBucket;
        this.referenceDir = referenceDir;
        this.referenceList = referenceList;
        this.alignDestGcsPrefix = alignDestGcsPrefix;
        this.sortDestGcsPrefix = sortDestGcsPrefix;
        this.mergeDestGcsPrefix = mergeDestGcsPrefix;
        this.memoryOutputLimit = memoryOutputLimit;
    }

    @Override
    public PCollection<KV<GeneReadGroupMetaData, GeneData>> expand(PCollection<KV<GeneExampleMetaData, GeneData>> input) {
        return input
                .apply(GroupByKey.create())
                .apply(ParDo.of(new AlignFn(
                        new CmdExecutor(),
                        new WorkerSetupService(new CmdExecutor()),
                        referenceList,
                        new IoHandler(srcBucket, destBucket, referenceDir, alignDestGcsPrefix, memoryOutputLimit)
                )))

                .apply(ParDo.of(new SortFn(
                        new IoHandler(destBucket, destBucket, referenceDir, sortDestGcsPrefix, memoryOutputLimit)
                )))
                .apply(MapElements.via(new SimpleFunction<KV<GeneExampleMetaData, GeneData>, KV<KV<GeneReadGroupMetaData, String>, GeneData>>() {
                    @Override
                    public KV<KV<GeneReadGroupMetaData, String>, GeneData> apply(KV<GeneExampleMetaData, GeneData> input) {
                        GeneExampleMetaData geneExampleMetaData = input.getKey();
                        GeneData geneData = input.getValue();
                        return KV.of(KV.of(geneExampleMetaData, geneData.getReferenceName()), geneData);
                    }
                }))
                .apply(GroupByKey.create())
                .apply(ParDo.of(new MergeFn(
                        new IoHandler(destBucket, destBucket, referenceDir, mergeDestGcsPrefix, memoryOutputLimit),
                        new BamFilesMerger()
                )));
    }
}
