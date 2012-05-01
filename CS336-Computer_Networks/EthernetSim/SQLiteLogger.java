/*
 * SQLiteLogger :
 *      This class uses the SQLite database package to record with high
 *      granularity the events of the ethernet simulation
 */

//package EthernetSimulator;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.RollingFileAppender;
import org.apache.log4j.NDC;

import java.sql.*;

public class SQLiteLogger
{
    private String dbLoc;
    private Logger logger = Logger.getLogger(SQLiteLogger.class);

    private Connection dbConn;

    public SQLiteLogger(String fileLoc)
    {
	try
	    {
		dbLoc = fileLoc;

		Class.forName("org.sqlite.JDBC");

		logger.info("Using database file: " + fileLoc);

		dbConn = DriverManager.getConnection("jdbc:sqlite:" + fileLoc);		

		logger.info("Database is now " + (dbConn.isClosed() ? "closed" : "open"));
	    }
	catch(SQLException e)
	    {
		logger.error(e.getMessage());
	    }
	catch(Exception generice)
	    {
		logger.error(generice.getMessage());
	    }
    }

    public void CreateNewEvent(int curExpID, double timeStart, double timeDuration, String evtType, int hostID, int isSelfEvt)
    {
	try
	    {
		PreparedStatement prepStat = dbConn.prepareStatement("INSERT INTO experiment_event VALUES (NULL, ?, ?, ?, ?, ?, ?)");

		prepStat.setInt(1, curExpID);
		prepStat.setDouble(2, timeStart);
		prepStat.setDouble(3, timeDuration);
		prepStat.setString(4, evtType);
		prepStat.setInt(5, hostID);
		prepStat.setInt(6, isSelfEvt);

		logger.info("creating new event");

		prepStat.executeUpdate();
	    }
	catch(SQLException e)
	    {
		logger.error(e.getMessage() + " error code: " + e.getErrorCode() + " SQL State: " + e.getSQLState());
	    }
    }

    public void CreateNewHost(int curExpID, int curHostPos)
    {
	try
	    {
		PreparedStatement prepStat = dbConn.prepareStatement("INSERT INTO experiment_hosts VALUES (NULL, ?, ?)");

		prepStat.setInt(1, curExpID);
		prepStat.setInt(2, curHostPos);

		logger.info("creating new host");

		prepStat.executeUpdate();
	    }
	catch(SQLException e)
	    {
		logger.error(e.getMessage() + " error code: " + e.getErrorCode() + " SQL State: " + e.getSQLState());
	    }
    }

    public int CreateNewExperiment(long curUnixTime, String curRunDateTime, int numHosts)
    {
	try
	    {
		PreparedStatement prepStat = dbConn.prepareStatement("INSERT INTO experiment VALUES (NULL, ?, ?, ?)");

		Long converter = new Long(curUnixTime);

		prepStat.setInt(1, converter.intValue());
		prepStat.setString(2, curRunDateTime);
		prepStat.setInt(3, numHosts);

		logger.info("creating new experiment");

		prepStat.executeUpdate();

		return getLastInsertRowID();
	    }
	catch(SQLException e)
	    {
		logger.error(e.getMessage() + " error code: " + e.getErrorCode() + " SQL State: " + e.getSQLState());

		return -1;
	    }
	catch(Exception generice)
	    {
		logger.error(generice.getMessage());

		return -1;
	    }
    }

    public void CreateNewExperimentSummary(int expID, int activeHosts, int packSize, int packetsSent, int totalBitsSent,
					   double experimentDuration, double avgTransDelay, double fairnessInd)
    {
	try
	    {
		PreparedStatement prepStat = dbConn.prepareStatement("INSERT INTO experiment_summary VALUES (NULL, ?, ?, ?, ?, ?, ?, ?, ?)");

		prepStat.setInt(1, expID);
		prepStat.setInt(2, activeHosts);
		prepStat.setInt(3, packSize);
		prepStat.setInt(4, packetsSent);
		prepStat.setInt(5, totalBitsSent);
		prepStat.setDouble(6, experimentDuration);
		prepStat.setDouble(7, avgTransDelay);
		prepStat.setDouble(8, fairnessInd);

		prepStat.executeUpdate();
	    }
	catch(SQLException e)
	    {
		logger.error(e.getMessage() + " error code: " + e.getErrorCode() + " SQL State: " + e.getSQLState());
	    }
	catch(Exception generice)
	    {
		logger.error(generice.getMessage());
	    }
    }


    public int getLastInsertRowID()
    {
	try
	    {
		Statement queryStat = dbConn.createStatement();

		ResultSet idRS = queryStat.executeQuery("SELECT last_insert_rowid();");

		int returnIDVal = -1;

		if(idRS.next())
		    {
			//logger.info("id returned from db query: " + idRS.getInt(1));

			returnIDVal = idRS.getInt(1);
		    }
		
		idRS.close();

		return returnIDVal;
	    }
	catch(SQLException e)
	    {
		logger.error(e.getMessage() + " error code: " + e.getErrorCode() + " SQL State: " + e.getSQLState());

		return -1;
	    }
	catch(Exception generice)
	    {
		logger.error(generice.getMessage());

		return -1;
	    }
    }

    


    public void close()
    {
	try
	    {
		logger.info("closing database connection to " + dbLoc);

		dbConn.close();
	    }
	catch(SQLException e)
	    {
		logger.error(e.getMessage() + "error code: " + e.getErrorCode() + " SQL State: " + e.getSQLState());
	    }
	catch(Exception generice)
	    {
		logger.error(generice.getMessage());
	    }
    }
}