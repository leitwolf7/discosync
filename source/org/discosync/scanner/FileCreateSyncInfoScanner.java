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
import java.sql.SQLException;
import java.util.Collection;
import java.util.zip.Adler32;

import org.apache.commons.io.*;
import org.discosync.data.FileListDatabase;

/*
 * Created on 01.04.2015
 */
public class FileCreateSyncInfoScanner extends DirectoryWalker {

    Adler32 adlerChecksum = new Adler32();

    String baseDir = null;
    int baseDirLength = -1;
    FileListDatabase database = null;

    public FileCreateSyncInfoScanner() {
        super();
    }

    public void scan(File startDirectory, FileListDatabase db) throws IOException {

        baseDir = startDirectory.getAbsolutePath();
        baseDirLength = baseDir.length();
        this.database = db;

        walk(startDirectory, null);
    }

    protected boolean handleDirectory(File directory, int depth, Collection results) {
        return true;
    }

    protected void handleFile(File file, int depth, Collection results) {

        long checksum = -1;

        adlerChecksum.reset();

        try {
            checksum = FileUtils.checksum(file, adlerChecksum).getValue();
        } catch (IOException e) {
            e.printStackTrace();
        }

        long size = file.length();

        // remove baseDir part
        String relativeName = file.getAbsolutePath().substring(baseDirLength);

        try {
            database.insertFile(relativeName, checksum, size);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // System.out.println("File: "+relativeName+"; checksum="+checksum+"; size="+size);
    }
}