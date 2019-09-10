package com.ms3.codingchallenge;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CodingChallengeApplicationTests {

	/*
	 * Tests the db generated from the sample CSV. 
	 */
	@Test
	public void test1() {
		
		String url = "jdbc:sqlite:data/databases/Jr. Coding Challenge Page 2.db";
		
		try (Connection conn = DriverManager.getConnection(url);
				Statement stmt  = conn.createStatement();
				ResultSet rs    = stmt.executeQuery("SELECT * FROM contents;")){
			
			int recordCount = 0;
		        
			// loop through the result set
			while (rs.next()) {
				assertTrue(rs.getString("A").trim().length() > 0 &&
						rs.getString("B").trim().length() > 0 &&
						rs.getString("C").trim().length() > 0 &&
						rs.getString("D").trim().length() > 0 &&
						rs.getString("E").trim().length() > 0 &&
						rs.getString("F").trim().length() > 0 &&
						rs.getString("G").trim().length() > 0 &&
						rs.getString("H").trim().length() > 0 &&
						rs.getString("I").trim().length() > 0 &&
						rs.getString("J").trim().length() > 0);
				recordCount++;
		    }
			assertTrue(recordCount == 4866);
		} catch (SQLException e){
			System.out.println(e.getMessage());
			fail();
		}
	}
	
	/* 
	 * I also ran the csv of invalid records through the application to confirm 
	 * that 0 records were successful, and the resulting csv of bad records is 
	 * verified as well.
	 */
	
	@Test
	public void test2() {
		
		String filePath = "data/logs/Jr. Coding Challenge Page 2-bad-bad.csv";
		
		try (BufferedReader reader = new BufferedReader(new FileReader(filePath))){
			
			int recordCount = 0;
		    
			//Skip header line 
			String line = reader.readLine();
			
			line = reader.readLine();
			
			while (line != null) {
				recordCount++;
				String[] vals = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1); 
				assertTrue("failure at record " + recordCount,
						vals.length != 10  ||
						vals[0].trim().length() == 0 ||
						vals[1].trim().length() == 0 ||
						vals[2].trim().length() == 0 ||
						vals[3].trim().length() == 0 ||
						vals[4].trim().length() == 0 ||
						vals[5].trim().length() == 0 ||
						vals[6].trim().length() == 0 ||
						vals[7].trim().length() == 0 ||
						vals[8].trim().length() == 0 ||
						vals[9].trim().length() == 0);
				line = reader.readLine();
		    }
			System.out.println(recordCount);
			assertTrue(recordCount == 1136);
		} catch (IOException e){
			System.out.println(e.getMessage());
			fail();
		}
	}

}
