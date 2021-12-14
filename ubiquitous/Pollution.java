package com.sajadian.ubiquitous;

import java.text.DecimalFormat;

import android.location.Location;

public class Pollution {
	private int id;
	private Location location;
	private int pollution;
	private int rate;
	private String date;
	private int blood1;
	private int blood2;
	private Boolean vital1=false;
	private Boolean vital2=false;
	private Boolean vital3=false;
	private Boolean vital4=false;
	private Boolean sign1=false;
	private Boolean sign2=false;
	private Boolean sign3=false;
	private Boolean sign4=false;
	private Boolean sign5=false;
	private Boolean sign6=false;
	private Boolean sign7=false;
	private String signs;
	private String blood;

	public int getID() {
		return id;
	}

	public void setID(int id) {
		this.id = id;
	}

	public String getLocation() {
		if (location == null)
			return null;
		return location.getLatitude() + "," + location.getLongitude();
	}

	public double getLat() {
		if (location == null)
			return 0;
		return location.getLatitude();
	}

	public double getLng() {
		if (location == null)
			return 0;
		return location.getLongitude();
	}

	public void setLocation(String location) {
		String[] l = location.split(",");
		Location loc = new Location("");
		loc.setLatitude(Double.parseDouble(l[0]));
		loc.setLongitude(Double.parseDouble(l[1]));
		this.location = loc;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public int getPollution() {
		return pollution;
	}

	public void setPollution(int pollution) {
		this.pollution = pollution;
	}

	public int getRate() {
		return rate;
	}

	public void setRate(int rate) {
		this.rate = rate;
	}

	public void setBlood1(int blood1) {
		this.blood1 = blood1;
	}

	public void setBlood2(int blood2) {
		this.blood2 = blood2;
	}
	
	public String getBlood() {
		return blood;
	}

	public String saveBlood() {
		return (blood1==0?"-":String.valueOf(blood1)) + "," + 
			   (blood2==0?"-":String.valueOf(blood2));
	}

	public void setBlood(String blood) {
		this.blood=blood;
	}

	public void setVital1(Boolean vital1) {
		this.vital1 = vital1;
	}

	public void setVital2(Boolean vital2) {
		this.vital2 = vital2;
	}

	public void setVital3(Boolean vital3) {
		this.vital3 = vital3;
	}

	public void setVital4(Boolean vital4) {
		this.vital4 = vital4;
	}

	public void setSign1(Boolean sign1) {
		this.sign1 = sign1;
	}
	
	public void setSign2(Boolean sign2) {
		this.sign2 = sign2;
	}

	public void setSign3(Boolean sign3) {
		this.sign3 = sign3;
	}

	public void setSign4(Boolean sign4) {
		this.sign4 = sign4;
	}

	public void setSign5(Boolean sign5) {
		this.sign5 = sign5;
	}

	public void setSign6(Boolean sign6) {
		this.sign6 = sign6;
	}

	public void setSign7(Boolean sign7) {
		this.sign7 = sign7;
	}

	public String getDate() {
		return date.toString();
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String saveSigns() {
		signs = "";
		signs += vital1 ? "Difficult Breathing \n" : "";
		signs += vital2 ? "Fainting \n" : "";
		signs += vital3 ? "Dizziness \n" : "";
		signs += vital4 ? "Chest Pain \n" : "";
		signs += sign1 ? "Jaw Pain \n" : "";
		signs += sign2 ? "Arm Pain \n" : "";
		signs += sign3 ? "Back Pain \n" : "";
		signs += sign4 ? "Abdominal Pain \n" : "";
		signs += sign5 ? "Sweating \n" : "";
		signs += sign6 ? "Vomiting \n" : "";
		signs += sign7 ? "Apprehension \n" : "";
		signs=signs.equals("")?"":signs;
		return signs;
	}
	
	public String getSigns(){
		return signs;
	}

	public void setSigns(String signs) {
		this.signs = signs;
	}

}
