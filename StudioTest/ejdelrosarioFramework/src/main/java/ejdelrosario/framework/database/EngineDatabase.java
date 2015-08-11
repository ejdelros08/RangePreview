/**
 * Created by EJ Del Rosario
 * Copyright (c) 2015
 * Personal Intellectual Property
 * All Rights Reserved
 */

package ejdelrosario.framework.database;

import java.util.ArrayList;

public class EngineDatabase {

	private String DBName;
	private int DBVersion;
	private ArrayList<Table> tables = new ArrayList<Table>();
	
	public EngineDatabase(String dbname , int dbversion) {
		DBName = dbname;
		DBVersion = dbversion;
	}
	
	
	public String getName() {
		return DBName;
	}
	
	
	public int getVersion() {
		return DBVersion;
	}
	
	
	public ArrayList<Table> getTables() {
		return tables;
	}
	
	
	public EngineDatabase addTable(Table table) {
		tables.add(table);
		return this;
	}
}
