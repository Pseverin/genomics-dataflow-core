package com.google.allenday.nanostream.gene;

import org.apache.beam.sdk.coders.DefaultCoder;
import org.apache.beam.sdk.coders.SerializableCoder;

import javax.annotation.Nullable;
import java.io.Serializable;

public abstract class GeneData implements Serializable {

    protected DataType dataType;
    protected String fileName;
    @Nullable
    protected String referenceName;

    public GeneData(DataType dataType, String fileName) {
        this.dataType = dataType;
        this.fileName = fileName;
    }

    public GeneData(DataType dataType, String fileName, @Nullable String referenceName) {
        this.dataType = dataType;
        this.fileName = fileName;
        this.referenceName = referenceName;
    }

    public abstract String getData();

    public abstract byte[] getDataAsBytes();

    public DataType getDataType() {
        return dataType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setReferenceName(String referenceName) {
        this.referenceName = referenceName;
    }

    @Nullable
    public String getReferenceName() {
        return referenceName;
    }

    @DefaultCoder(SerializableCoder.class)
    public enum DataType {
        RAW, BLOB_URI
    }

    @Override
    public String toString() {
        return "GeneData{" +
                "dataType=" + dataType +
                ", fileName='" + fileName + '\'' +
                ", referenceName='" + referenceName + '\'' +
                '}';
    }
}
