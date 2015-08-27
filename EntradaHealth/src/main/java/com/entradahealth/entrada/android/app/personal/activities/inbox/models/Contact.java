package com.entradahealth.entrada.android.app.personal.activities.inbox.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Contact implements Comparable<Contact>, Parcelable{
	
	  public static final int ITEM = 0;
	  public static final int SECTION = 1;
	  
	  public final int type;
	  public final String text;
	  public final String no;
	  public String email;
	  
	  public Contact(int type, String text, String no) {
	   this.type = type;
	   this.text = text;
	   this.no = no;
	  }
	  
	  public Contact(int type, String text, String no, String email) {
		   this.type = type;
		   this.text = text;
		   this.no = no;
		   this.email = email;
		  }
	  
	  public String getContactName(){
		  return text;
	  }
	  
	  public String getContactNo(){
		  return no;
	  }
	  
	  public String getEmail(){
		  return email;
	  }
	  
	  @Override 
	  public String toString() {
		  return text;
	  }
	  
	  public String getAll(){
		  String contact = text+"/"+no;
		  return contact;
	  }

	@Override
	public int compareTo(Contact contact) {
		 return text.compareToIgnoreCase(contact.text);
	}

	public static final Parcelable.Creator<Contact> CREATOR = new Parcelable.Creator<Contact>() {
	    public Contact createFromParcel(Parcel in) {
	     return new Contact(in);
	    }

	    public Contact[] newArray(int size) {
	     return new Contact[size];
	    }
	  };

	  @Override
	  public int describeContents() {
	    return 0;
	  } 
	  
	  private Contact(Parcel in) {
		  	type = in.readInt();
		    text = in.readString();
		    no = in.readString();
	  }
	  
	  @Override
	  public void writeToParcel(Parcel dest, int flags) {
	    dest.writeInt(type);
	    dest.writeString(text);
	    dest.writeString(no);
	  }
	}
