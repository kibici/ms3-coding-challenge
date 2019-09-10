package com.ms3.codingchallenge;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DbGenerator {
	
	static final String DB_DIR = "data/databases/";
	static final String LOG_DIR = "data/logs/";
	
	/*
	 *  Number of records to be inserted into the database per insertion query; 
	 *  grouping records to be inserted improves performance up until about 50 
	 *  records per query, after which performance begins to decline (see 
	 *  rpq-test-results.png in the data folder). 
	 */
	static final int REC_PER_QUERY = 50;
	static final int NUM_COL = 10;
	
	private String filePath;
	private String fileName; 
	private String invalidRecs;
	private int numRcvd, numSucc, numFail;
	private long startTime, endTime;
	
	/*
	 * Constructor takes filepath for input csv
	 */
	public DbGenerator(String filePath) {
		
		this.filePath = filePath;
		
		// get file name (without extension)
		int fnStart = Math.max(filePath.lastIndexOf("/") + 1, 0);
		int fnEnd = filePath.lastIndexOf(".csv");
		if (fnEnd == -1 || fnEnd < fnStart) 
			fnEnd = filePath.length();
		
		fileName = filePath.substring(fnStart, fnEnd);
		invalidRecs = "A,B,C,D,E,F,G,H,I,J\n";
		numRcvd = numSucc = numFail = 0;
	}
	
	/*
	 * Creates a new database with the filename of the csv specified in the constructor
	 * or connects to a prexisting db with the same name. All valid records (containing
	 * values for every column) from the csv file are inserted into a 'contents' 
	 * table within the db (if the table already exists it is overwritten). Saves
	 * log data and a csv file with all invalid records.
	 * 
	 * Returns 
	 */
	public void createDB() {
		
		startTime = System.currentTimeMillis();
		 
        String url = "jdbc:sqlite:" + DB_DIR + fileName + ".db";
        
        // create new db or connect to db if one with the same name already exists 
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath));
        		Connection conn = DriverManager.getConnection(url);
        		Statement stmt = conn.createStatement()) {

            System.out.println("\nNew database '" + fileName + ".db' has been created.");      

            String line = reader.readLine();
            
            //  check that csv contains correct number of headers/columns
            if (line.split(",").length != NUM_COL) {
            	System.out.println("File does not have proper CSV format or there "
            			+ "is an invalid number of header values in the CSV file.");
            	return;
            }

            // if db already existed and contains 'contents' table, drop the table
            stmt.execute("DROP TABLE IF EXISTS contents;");
            stmt.execute("CREATE TABLE contents (\n"
                    + "    A text NOT NULL,\n"
                    + "    B text NOT NULL,\n"
                    + "    C text NOT NULL,\n"
                    + "    D text NOT NULL,\n"
                    + "    E text NOT NULL,\n"
                    + "    F text NOT NULL,\n"
                    + "    G real NOT NULL,\n"
                    + "    H text NOT NULL,\n"
                    + "    I text NOT NULL,\n"
                    + "    J text NOT NULL\n"
                    + ");");
           
			String queryVals = "", queryPrefix = 
         		"INSERT INTO contents (A,B,C,D,E,F,G,H,I,J) VALUES\n";
	
			// process lines from csv
			line = reader.readLine();
			while (line != null) {
				numRcvd++;
				String processedLine = processRec(line);
				// invalid record
				if (processedLine == null) {
					invalidRecs += line + "\n";
					numFail++;
				} else {
					queryVals += "(" + processedLine + ")";
					numSucc++;
					if (numSucc % REC_PER_QUERY == 0) {
						queryVals += ";";
						stmt.execute(queryPrefix + queryVals);
						queryVals = "";
					} else {
						queryVals += ",\n";
					}
				}
				line = reader.readLine();
			}
			
			// perform final insertion query (need to modify trailing characters)
			if (queryVals.length() != 0) {
    			stmt.execute(
        				queryPrefix + 
    					queryVals.substring(0, queryVals.trim().length() - 1) + 
    					";");
			}
			
			endTime = System.currentTimeMillis();
			logResults();	
            
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } catch (IOException e) {
        	System.out.println(e.getMessage());
		}
	}
	
	/*
	 * Logs and saves results of csv content insertion into database, as well as 
	 * a csv file of all invalid records  
	 */
	private void logResults() {
		
		String log = "Records received: " + numRcvd +
					 "\nRecords successful: " + numSucc + 
					 "\nRecords failed: " + numFail + 
					 String.format("\nCompleted in %.03f seconds.", (double)(endTime-startTime)/1000) +
					 "\n\nFailed records written to " + fileName + "-bad.csv";
		
		System.out.println("\n" + log);
		
		try (BufferedWriter writer = 
				new BufferedWriter(new FileWriter(LOG_DIR + fileName + ".log")))
		{
		    writer.write(log);
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		
		try (BufferedWriter writer = 
				new BufferedWriter(new FileWriter(LOG_DIR + fileName + "-bad.csv"))) 
		{
		    writer.write(invalidRecs);
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		
		System.out.println("Log file generated.\n");
	}
	
	/*
	 * Helper: takes a raw line of text from a csv file and checks that it has the 
	 * correct number of values. Returns a string formated for an insertion 
	 * query, or null if the record is invalid.
	 */
	private static String processRec(String line) {
		
		// only split on commas that aren't within quotes 
		String[] cols = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
		
		/* 
		 * Just thought of a better way to do this, but I don't quite have enough
		 * time to implement it: Use a regex that only splits on commas that aren't 
		 * within quotes OR immediately next to another comma, then all I have to 
		 * do is check the size of the resulting list.
		 */
		
		if (cols.length != NUM_COL)
			return null;
		// if a column value is empty, return null, o.w. format value for insertion
		for(int i = 0 ; i < cols.length ; i++) {
			if (cols[i].trim().length() == 0) 
				return null;
			else cols[i] = "'" + cols[i].replace("'", "''") + "'";
		}
		return String.join(",", cols);
	}

}
