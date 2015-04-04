/*
 * Created on 04.04.2015
 */
package org.discosync;

import java.io.IOException;
import java.sql.SQLException;

import org.apache.commons.cli.CommandLine;

public interface IInvokable {
    
    public boolean invoke(CommandLine cmd) throws SQLException, IOException;
}
