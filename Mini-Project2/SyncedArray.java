//package MiniProject2;

import java.util.ArrayList;

public class SyncedArray {
	
	private static ArrayList<String> myArray;

	public SyncedArray(){
		
		myArray = new ArrayList<String>();
		
	}
	
	public synchronized void add(String item){
		myArray.add(item);
	}
	
	public synchronized boolean remove(String item){
		boolean success =myArray.remove(item);
		return success;
	}
	
	public synchronized boolean contains(String item){
		if (myArray.contains(item)) return true;
		else return false;
	}
	
	public synchronized ArrayList<String> getAll(){
		ArrayList<String> newArray = new ArrayList<>(myArray);
		return newArray;
	}
}
