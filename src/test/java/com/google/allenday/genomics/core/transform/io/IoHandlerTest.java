package com.google.allenday.genomics.core.transform.io;

import com.google.allenday.genomics.core.gene.GeneData;
import com.google.allenday.genomics.core.io.FileUtils;
import com.google.allenday.genomics.core.io.GCSService;
import com.google.allenday.genomics.core.io.IoHandler;
import com.google.cloud.storage.Blob;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;

public class IoHandlerTest {

    private final static String RESULT_BUCKET = "resultsBucket";
    private final static String DEST_GCS_PREFIX = "destGcsPrefix";
    private final static String WORK_DIR = "workDir";
    private final static String FILE_NAME = "fileName";
    private final static String BLOB_URI = "blobUri";

    @Test
    public void testHandleInputAsLocalFileResult() {
        FileUtils fileUtilsMock = Mockito.mock(FileUtils.class, Mockito.withSettings().serializable());
        GCSService gcsServiceMock = Mockito.mock(GCSService.class, Mockito.withSettings().serializable());
        GeneData geneDataMock = Mockito.mock(GeneData.class, Mockito.withSettings().serializable());

        Mockito.when(geneDataMock.getFileName()).thenReturn(FILE_NAME);

        IoHandler ioHandler = new IoHandler(RESULT_BUCKET, DEST_GCS_PREFIX, 100, fileUtilsMock);
        String result = ioHandler.handleInputAsLocalFile(gcsServiceMock, geneDataMock, WORK_DIR);

        Assert.assertEquals("Result asserting", WORK_DIR + FILE_NAME, result);
    }

    @Test
    public void testHandleInputAsLocalFileBlobUriBranch() {
        FileUtils fileUtilsMock = Mockito.mock(FileUtils.class, Mockito.withSettings().serializable());
        GCSService gcsServiceMock = Mockito.mock(GCSService.class, Mockito.withSettings().serializable());
        GeneData geneDataMock = Mockito.mock(GeneData.class, Mockito.withSettings().serializable());
        Blob blobMock = Mockito.mock(Blob.class, Mockito.withSettings().serializable());

        Mockito.when(geneDataMock.getFileName()).thenReturn(FILE_NAME);
        Mockito.when(geneDataMock.getDataType()).thenReturn(GeneData.DataType.BLOB_URI);
        Mockito.when(geneDataMock.getBlobUri()).thenReturn(BLOB_URI);
        Mockito.when(gcsServiceMock.getBlob(Mockito.any())).thenReturn(blobMock);

        IoHandler ioHandler = new IoHandler(RESULT_BUCKET, DEST_GCS_PREFIX, 100, fileUtilsMock);
        ioHandler.handleInputAsLocalFile(gcsServiceMock, geneDataMock, WORK_DIR);

        Mockito.verify(gcsServiceMock).getBlobIdFromUri(BLOB_URI);
        Mockito.verify(gcsServiceMock).downloadBlobTo(blobMock, WORK_DIR + FILE_NAME);
    }

    @Test
    public void testHandleInputAsLocalFileContentBranch() throws IOException {
        FileUtils fileUtilsMock = Mockito.mock(FileUtils.class, Mockito.withSettings().serializable());
        GCSService gcsServiceMock = Mockito.mock(GCSService.class, Mockito.withSettings().serializable());
        GeneData geneDataMock = Mockito.mock(GeneData.class, Mockito.withSettings().serializable());

        byte[] byteArray = "content".getBytes();

        Mockito.when(geneDataMock.getFileName()).thenReturn(FILE_NAME);
        Mockito.when(geneDataMock.getDataType()).thenReturn(GeneData.DataType.CONTENT);
        Mockito.when(geneDataMock.getContent()).thenReturn(byteArray);

        IoHandler ioHandler = new IoHandler(RESULT_BUCKET, DEST_GCS_PREFIX, 100, fileUtilsMock);
        ioHandler.handleInputAsLocalFile(gcsServiceMock, geneDataMock, WORK_DIR);

        Mockito.verify(fileUtilsMock).saveDataToFile(byteArray, WORK_DIR + FILE_NAME);
    }
}
