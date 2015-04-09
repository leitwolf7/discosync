/*
 * This file is part of DiscoSync (home: github.com, leitwolf7/discosync)
 * 
 * Copyright (C) 2015, 2015 leitwolf7
 *
 *  DiscoSync is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  DiscoSync is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with DiscoSync.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.discosync.data;

/*
 * Created on 01.04.2015
 */
public class FileListEntry {
    protected String path;
    protected boolean isDirectory;
    protected long checksum; 
    protected long size;
    protected FileOperations operation = FileOperations.KEEP;

    /**
     * Create a new entry for a DIRECTORY.
     * @param path
     */
    public FileListEntry(String path) {
        this.path = path;
        this.isDirectory = true;
        this.checksum = 0;
        this.size = 0;
    }

    /**
     * Create a new entry for a FILE.
     * @param path
     * @param checksum
     * @param size
     */
    public FileListEntry(String path, long checksum, long size) {
        this.path = path;
        this.isDirectory = false;
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

    public boolean isDirectory() {
        return isDirectory;
    }
}