package com.example.recloc.db;

import java.math.BigInteger;  
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;  

public abstract class UUIDGenerator {  
	
	public static String generator() {  

		UUID uuid = UUID.randomUUID();  
		String sud = uuid.toString();  
		sud = sud.replaceAll("-", "");  
		BigInteger integer = new BigInteger(sud, 16);  
		return integer.toString().substring(0, 32);  
	}  

	public static String genTimeStamp(){
		Date tDate = new Date();
		Calendar calendar = Calendar.getInstance();
		tDate = calendar.getTime();
		return Long.toString(tDate.getTime());
	}

}  
