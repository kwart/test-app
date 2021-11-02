package com.hazelcast.maven.attribution;

public class SrcFile {
    private final String archiveName;
    private final String sourceName;
    private final byte[] bytes;

    public SrcFile(String archiveName, String sourceName, byte[] bytes) {
        this.archiveName = archiveName;
        this.sourceName = sourceName;
        this.bytes = bytes;
    }

    public String getArchiveName() {
        return archiveName;
    }

    public String getSourceName() {
        return sourceName;
    }

    public byte[] getBytes() {
        return bytes;
    }
}
