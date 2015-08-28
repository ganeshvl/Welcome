package com.entradahealth.entrada.android.app.personal.activities.manage_queues;

public class QueuesListItem {
	 
	 private String description;
	 private long id;
	 private boolean isSubscribed;
	 private String name;
	 
	 /**
	  * @return the name of the item 
	  */
	 public String getDescription() {
	  return description;
	 }
	  
	 /**
	  * Set the name of the item
	  * 
	  * @param name The name of the item 
	  */
	 public void setdescription(String description) {
	  this.description = description;
	 }
	  
	 /**
	  * @return the name of the item 
	  */
	 public String getName() {
	  return name;
	 }
	  
	 /**
	  * Set the name of the item
	  * 
	  * @param name The name of the item 
	  */
	 public void setName(String name) {
	  this.name = name;
	 }
	  
	 /**
	  * @return the price of the item 
	  */
	 public long getId() {
	  return id;
	 }
	  
	 /**
	  * Set the price of the item
	  * 
	  * @param price The price of the item 
	  */
	 public void setId(long id) {
	  this.id = id;
	 }
	  
	 /**
	  * @return the quantity of the item 
	  */
	 public boolean getisSubscribed() {
	  return isSubscribed;
	 }
	  
	 /**
	  * Set the quantity of the item
	  * 
	  * @param quantity The quantity of the item 
	  */
	 public void setisSubscribed(boolean isSubscribed) {
	  this.isSubscribed = isSubscribed;
	 }
	 
	 
	}
