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

package org.discosync.scanner;

import java.io.*;
import java.util.*;
import java.util.zip.Adler32;

import org.apache.commons.io.*;
import org.discosync.Utils;
import org.discosync.data.*;

/*
 * Created on 01.04.2015
 */
public class FileCheckSyncInfoScanner extends DirectoryWalker {

    Adler32 adlerChecksum = new Adler32();

    String baseDir = null;
    int baseDirLength = -1;

    Map<String, FileListEntry> dstFileMap;
    List<FileListEntry> fileOperations;

    public FileCheckSyncInfoScanner() {
        super();
    }

    public void scan(File startDirectory, Map<String, FileListEntry> dstFileMap, List<FileListEntry> fileOperations)
        throws IOException {

        baseDir = startDirectory.getAbsolutePath();
        baseDirLength = baseDir.length();
        this.dstFileMap = dstFileMap;
        this.fileOperations = fileOperations;

        walk(startDirectory, null);
    }

    protected boolean handleDirectory(File directory, int depth, Collection results) {
        
        // remove baseDir part
        String relativeName = directory.getAbsolutePath().substring(baseDirLength);
        
        FileListEntry e = new FileListEntry(relativeName);
        
        Utils.doFileListEntryCompare(e, dstFileMap, fileOperations);
        
        return true;
    }

    protected void handleFile(File file, int depth, Collection results) {

        long size = file.length();

        // remove baseDir part
        String relativeName = file.getAbsolutePath().substring(baseDirLength);
        
        // check if we can avoid to generate the checksum
        FileListEntry dstEntry = dstFileMap.get(relativeName);
// FIXME: always create checksum to be able to compare
        if (dstEntry != null) {
            // file exists in dest, check size, ckSum
            if (size != dstEntry.getSize()) {
                // file exists, but is size different -> REPLACE
                // put an entry without checksum
                FileListEntry e = new FileListEntry(relativeName, 0L, size);
                e.setOperation(FileOperations.REPLACE);
                fileOperations.add(e);
                // remove processed entry
                dstFileMap.remove(dstEntry.getPath());
                
                return;
            } else {
                // same size, we must generate the checksum
            }
        } else {
            // file is missing in dest -> copy
            // put an entry without checksum
            FileListEntry e = new FileListEntry(relativeName, 0L, size);
            e.setOperation(FileOperations.COPY);
            fileOperations.add(e);
            return;
        }
        
        long checksum = -1;
        try {
            adlerChecksum.reset();
            checksum = FileUtils.checksum(file, adlerChecksum).getValue();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        FileListEntry e = new FileListEntry(relativeName, checksum, size);

        Utils.doFileListEntryCompare(e, dstFileMap, fileOperations);

        // System.out.println("File: "+relativeName+"; checksum="+checksum+"; size="+size);
    }
}