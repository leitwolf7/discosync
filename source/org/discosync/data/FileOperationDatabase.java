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
        String sqlDrop = "DROP TABLE fileoperation IF EXISTS";
        
        stmt.execute(sqlDrop);
        
        String sqlCreate = "CREATE TABLE IF NOT EXISTS fileoperation "
                + "  (filepath        VARCHAR(265),"
                + "   isdirectory     BOOLEAN,"
                + "   size            BIGINT,"
                + "   oldchecksum     BIGINT," // checksum of the file to be replaced
                + "   operation       VARCHAR(16))";

        stmt.execute(sqlCreate);
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
            insertStatement = stmt.getConnection().prepareStatement("INSERT INTO fileoperation (filepath,size,operation) VALUES (?,?,?)");
        }
        return insertStatement;
    }

    public List<FileListEntry> retrieveFileOperations() throws SQLException {
        List<FileListEntry> fileList = new ArrayList<>();
        ResultSet rs = stmt.executeQuery("SELECT filepath,size,operation FROM fileoperation");
        while (rs.next()) {
            String path = rs.getString(1);
            long size = rs.getLong(2);
            FileOperations op = FileOperations.valueOf(rs.getString(3));
            FileListEntry e = new FileListEntry(path, 0L, size); // FIXME retrieve correct infos
            e.setOperation(op);
            fileList.add(e);
        }
        rs.close();

        return fileList;
    }
    
    public Iterator<FileListEntry> getFileListOperationIterator() throws SQLException {
        ResultSet rs = stmt.executeQuery("SELECT filepath,size,operation FROM fileoperation");
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
                String path = resultSet.getString(1);
                long size = resultSet.getLong(2);
                FileOperations op = FileOperations.valueOf(resultSet.getString(3));
                entry = new FileListEntry(path, 0L, size);
                entry.setOperation(op);
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
