package org.discosync.data;

/*
 * Created on 01.04.2015
 */

public class FileListEntry {
    protected String path;
    protected long checksum; 
    protected long size;
    protected FileOperations operation = FileOperations.KEEP;
    
    public FileListEntry(String path, long checksum, long size) {
        this.path = path;
        this.checksum = checksum;
        this.size = size;
    }

    public String getPath() {
        return path;
    }

    public long getChecksum() {
        return checksum;
    }

    public long getSize() {
        return size;
    }
    
    public FileOperations getOperation() {
        return operation;
    }
    public void setOperation(FileOperations op) {
        operation = op;
    }
    
    public String toString() {
        return operation.toString() + ": " + path;
    }
}