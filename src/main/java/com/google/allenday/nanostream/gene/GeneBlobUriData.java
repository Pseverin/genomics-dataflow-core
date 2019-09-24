package com.google.allenday.nanostream.gene;

public class GeneBlobUriData extends GeneData {

    private String blobUri;

    public GeneBlobUriData(String fileName, String blobUri) {
        super(DataType.BLOB_URI, fileName);
        this.blobUri = blobUri;
    }

    public GeneBlobUriData(String fileName, String referenceName, String blobUri) {
        super(DataType.BLOB_URI, fileName, referenceName);
        this.blobUri = blobUri;
    }

    @Override
    public String getData() {
        return blobUri;
    }

    @Override
    public byte[] getDataAsBytes() {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public String toString() {
        return "GeneBlobUriData{" +
                "blobUri='" + blobUri + '\'' +
                ", dataType=" + dataType +
                ", fileName='" + fileName + '\'' +
                ", referenceName='" + referenceName + '\'' +
                '}';
    }
}
