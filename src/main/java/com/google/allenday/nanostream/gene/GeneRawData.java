package com.google.allenday.nanostream.gene;

public class GeneRawData extends GeneData {

    private byte[] raw;

    public GeneRawData(String fileName, byte[] raw) {
        super(DataType.RAW, fileName);
        this.raw = raw;
    }

    public GeneRawData(String fileName, String referenceName, byte[] raw) {
        super(DataType.RAW, fileName, referenceName);
        this.raw = raw;
    }

    @Override
    public String getData() {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public byte[] getDataAsBytes() {
        return raw;
    }

    @Override
    public String toString() {
        return "GeneRawData{" +
                "rawSize=" + raw.length +
                ", dataType=" + dataType +
                ", fileName='" + fileName + '\'' +
                ", referenceName='" + referenceName + '\'' +
                '}';
    }
}
