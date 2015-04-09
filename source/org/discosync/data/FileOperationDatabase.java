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
public class FileOperationDatabase {
    
    protected String name;
    protected Connection conn;
    protected Statement stmt;
    protected PreparedStatement insertStatement = null;
    
    protected final String SQL_DROP = "DROP TABLE fileoperation IF EXISTS";
    
    protected final String SQL_CREATE = "CREATE TABLE IF NOT EXISTS fileoperation "
            + "  (filepath        VARCHAR(265),"
            + "   isdirectory     BOOLEAN,"
            + "   size            BIGINT,"
            + "   oldchecksum     BIGINT," // checksum of the file to be replaced
            + "   operation       VARCHAR(16))";
    
    protected final String SQL_INSERT = "INSERT INTO fileoperation "
    		+ "(filepath,isdirectory,size,oldchecksum,operation) VALUES (?,?,?,?,?)";
    
    protected final String SQL_SELECT = "SELECT filepath,isdirectory,size,oldchecksum,operation FROM fileoperation";
    
    public FileOperationDatabase(String name) {
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
    
    public void createFileOperationTable() throws SQLException {
        stmt.execute(SQL_DROP);
        stmt.execute(SQL_CREATE);
    }
    
    public void insertFileOperations(List<FileListEntry> fileOperations) throws SQLException {
        PreparedStatement insertStmt = getInsertStatement();
        for (FileListEntry e : fileOperations) {
            insertStmt.setObject(1, e.getPath());
            if (e.isDirectory()) {
                insertStmt.setObject(2, Boolean.TRUE);
                insertStmt.setObject(3, 0L);
                insertStmt.setObject(4, 0L);
                insertStmt.setObject(5, e.getOperation().toString());
            } else {
                insertStmt.setObject(2, Boolean.FALSE);
                insertStmt.setObject(3, e.getSize());
                insertStmt.setObject(4, e.getChecksum());
                insertStmt.setObject(5, e.getOperation().toString());
                insertStmt.executeUpdate();
            }
        }
    }
    
    private PreparedStatement getInsertStatement() throws SQLException {
        if (insertStatement == null) {
            insertStatement = stmt.getConnection().prepareStatement(SQL_INSERT);
        }
        return insertStatement;
    }

    public List<FileListEntry> retrieveFileOperations() throws SQLException {
        List<FileListEntry> fileList = new ArrayList<>();
        ResultSet rs = stmt.executeQuery(SQL_SELECT);
        while (rs.next()) {
        	FileListEntry e = createFileListEntry(rs);
            fileList.add(e);
        }
        rs.close();

        return fileList;
    }
    
    protected FileListEntry createFileListEntry(ResultSet rs) throws SQLException {
        final FileListEntry e;
        
        String path = rs.getString(1);
        boolean isDirectory = rs.getBoolean(2);
        if (isDirectory) {
            e = new FileListEntry(path);
        } else {
            long oldchecksum = rs.getLong(3);
            long size = rs.getLong(4);
            e = new FileListEntry(path, oldchecksum, size);
        }
        FileOperations op = FileOperations.valueOf(rs.getString(5));
        e.setOperation(op);
        return e;
    }
    
    public Iterator<FileListEntry> getFileListOperationIterator() throws SQLException {
        ResultSet rs = stmt.executeQuery(SQL_SELECT);
        FileListEntryIterator it = new FileListEntryIterator(rs);
        return it;
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
            } catch(SQLException ex) {
                ex.printStackTrace();
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
