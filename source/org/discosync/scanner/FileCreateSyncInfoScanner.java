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

    protected Adler32 adlerChecksum = new Adler32();

    protected String baseDir = null;
    protected int baseDirLength = -1;
    protected FileListDatabase database = null;

    protected int entryCount = 0;
    protected long byteCount = 0L;

    public FileCreateSyncInfoScanner() {
        super();
    }

    /**
     * Scan the directory, create FileLiszEntry for each file and directory and add it to the database.
     *
     * @param startDirectory  base directory
     * @param db database to add the entries to
     */
    public void scan(File startDirectory, FileListDatabase db) throws IOException {

        baseDir = startDirectory.getAbsolutePath();
        baseDirLength = baseDir.length();
        this.database = db;

        walk(startDirectory, null);
    }

    @Override
    protected boolean handleDirectory(File directory, int depth, Collection results) {

        // remove baseDir part
        String relativeName = directory.getAbsolutePath().substring(baseDirLength);

        try {
            database.insertDirectory(relativeName);
            entryCount++;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return true;
    }

    @Override
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
            entryCount++;
            byteCount += size;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Entry count is set after the scanner finished.
     * @return number of entries added to database
     */
    public int getEntryCount() {
        return entryCount;
    }

    /**
     * Byte count is set after the scanner finished.
     * @return the number of processed bytes
     */
    public long getByteCount() {
        return byteCount;
    }
}