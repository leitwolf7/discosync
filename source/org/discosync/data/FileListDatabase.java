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

import java.sql.*;
import java.util.*;

/*
 * Created on 01.04.2015
 */
public class FileListDatabase {
    
    protected String name;
    protected Connection conn;
    protected Statement stmt;
    protected PreparedStatement insertStatement = null;

    public FileListDatabase(String name) {
        this.name = name;
    }

    public void open() throws SQLException {

        try {
            Class.forName("org.hsqldb.jdbc.JDBCDriver" );
        } catch (Exception e) {
            System.err.println("ERROR: failed to load HSQLDB JDBC driver.");
            e.printStackTrace();
        }

        conn = DriverManager.getConnection("jdbc:hsqldb:file:"+name+";shutdown=true", "SA", "");
        stmt = conn.createStatement();
    }
    
    public void close() {
        if (insertStatement != null) {
            try {
                insertStatement.close();
            } catch (SQLException e) {
            }
        }
        try {
            stmt.close();
        } catch (SQLException e) {
        }
        try {
            conn.close();
        } catch (SQLException e) {
        }
    }
    
    public void createFileListTable() throws SQLException {
        String sqlDrop = "DROP TABLE filelist IF EXISTS";
        
        stmt.execute(sqlDrop);
        
        String sqlCreate = "CREATE TABLE IF NOT EXISTS filelist "
                + "  (filepath        VARCHAR(265),"
                + "   isdirectory     BOOLEAN,"
                + "   checksum        BIGINT,"
                + "   size            BIGINT)";

        stmt.execute(sqlCreate);
    }
    
    public void insertFile(String path, long checksum, long size) throws SQLException {
        PreparedStatement insertStmt = getInsertStatement();
        insertStmt.setObject(1, path);
        insertStmt.setObject(2, Boolean.FALSE);
        insertStmt.setObject(3, checksum);
        insertStmt.setObject(4, size);
        insertStmt.executeUpdate();
    }
    
    public void insertDirectory(String path) throws SQLException {
        PreparedStatement insertStmt = getInsertStatement();
        insertStmt.setObject(1, path);
        insertStmt.setObject(2, Boolean.TRUE);
        insertStmt.setObject(3, 0L);
        insertStmt.setObject(4, 0L);
        insertStmt.executeUpdate();
    }
    
    private PreparedStatement getInsertStatement() throws SQLException {
        if (insertStatement == null) {
            insertStatement = stmt.getConnection().prepareStatement("INSERT INTO filelist (filepath,isdirectory,checksum,size) VALUES (?,?,?,?)");
        }
        return insertStatement;
    }

    public Map<String,FileListEntry> retrieveFileList() throws SQLException {
        Map<String,FileListEntry> fileMap = new HashMap<>();
        ResultSet rs = stmt.executeQuery("SELECT filepath,isdirectory,checksum,size FROM filelist");
        while (rs.next()) {
            FileListEntry e = createFileListEntry(rs);
            fileMap.put(e.getPath(), e);
        }
        rs.close();

        return fileMap;
    }
    
    public Iterator<FileListEntry> getFileListEntryIterator() throws SQLException {
        ResultSet rs = stmt.executeQuery("SELECT filepath,isdirectory,checksum,size FROM filelist");
        FileListEntryIterator it = new FileListEntryIterator(rs);
        return it;
    }
    
    protected FileListEntry createFileListEntry(ResultSet rs) throws SQLException {
        final FileListEntry e;
        
        String path = rs.getString(1);
        boolean isDirectory = rs.getBoolean(2);
        if (isDirectory) {
            e = new FileListEntry(path);
        } else {
            long checksum = rs.getLong(3);
            long size = rs.getLong(4);
            e = new FileListEntry(path, checksum, size);
        }
        return e;
    }
    
    protected class FileListEntryIterator implements Iterator<FileListEntry> {
        
        protected ResultSet resultSet;
        protected boolean hasNext;
        
        public FileListEntryIterator(ResultSet rs) throws SQLException {
            resultSet = rs;
            // pos to first entry
            hasNext = resultSet.next();
        }

        @Override
        public boolean hasNext() {
            return hasNext;
        }

        @Override
        public FileListEntry next() {
            FileListEntry entry = null;
            try {
                entry = createFileListEntry(resultSet);
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            
            // pos to next
            try {
                hasNext = resultSet.next();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            
            return entry;
        }

        /**
         * Not implemented!
         */
        @Override
        public void remove() {
        }
    }
}
