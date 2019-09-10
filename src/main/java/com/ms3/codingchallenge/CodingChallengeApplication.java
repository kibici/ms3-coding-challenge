package com.ms3.codingchallenge;

import java.util.Scanner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.ms3.codingchallenge.DbGenerator;

@SpringBootApplication
public class CodingChallengeApplication {
	private static boolean SINGLE_RUN = false; 

	public static void main(String[] args) {
		
		if (SINGLE_RUN) {
			DbGenerator dbGen = new DbGenerator("data/Jr. Coding Challenge Page 2.csv");
			dbGen.createDB();
			return;
		}
		
		Scanner scanner = new Scanner(System.in);
		
		while(true) {
			
			System.out.println(
					"Please enter the file path of a csv file you wish "
					+ "to use to generate a new database, or enter 'quit' to "
					+ "terminate the program: ");
			
			String input = scanner.nextLine();

			if (input.equals("quit")) {
				break;
			} else {
				DbGenerator dbGen = new DbGenerator(input);
				dbGen.createDB();
			}	
		}
		scanner.close();
	}
}
