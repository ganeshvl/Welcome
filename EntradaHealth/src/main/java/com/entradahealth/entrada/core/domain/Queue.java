package com.entradahealth.entrada.core.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents an Entrada queue, to which jobs can be assigned. (Job.queue
 * maps to Queue.name.)
 *
 * @author edr
 * @since 28 Aug 2012
 */
public final class Queue implements Comparable<Queue>{
	public static final String USER_QUEUE_SUBSCRIPTIONS = "queue_subscriptions";

	public static final String FIELD_ID = "QueueID";
	public static final String FIELD_NAME = "Name";
	public static final String FIELD_DESCRIPTION = "Description";
	public static final String FIELD_ISSUBSCRIBED = "IsSubscribed";
	// public static final String FIELD_ISDICTATORQUEUE = "IsDictatorQueue";

    public final long id;
    public final String name;
    public final String description;
    
    
    // This will be null if the Queue is loaded from the database.
    // Check the Account under the setting Queue.USER_QUEUE_SUBSCRIPTIONS
    public Boolean isSubscribed;
    //public Boolean isDictatorQueue;
    
    @JsonCreator
    public Queue(@JsonProperty(FIELD_ID) long id,
                 @JsonProperty(FIELD_NAME) String name,
                 @JsonProperty(FIELD_DESCRIPTION) String description,
                 @JsonProperty(FIELD_ISSUBSCRIBED) Boolean isSubscribed)
                 //@JsonProperty(FIELD_ISDICTATORQUEUE) Boolean isDictatorQueue)
                 
    {
        this.id = id;
        this.name = name;
        this.description = description;
        this.isSubscribed = isSubscribed;
        //this.isDictatorQueue = isDictatorQueue;
        
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Queue queue = (Queue) o;

        if (id != queue.id)
            return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        return (int) (id ^ (id >>> 32));
    }

	@Override
	public int compareTo(Queue q) {
		// TODO Auto-generated method stub
		return this.name.compareTo(q.name);
	}
}
