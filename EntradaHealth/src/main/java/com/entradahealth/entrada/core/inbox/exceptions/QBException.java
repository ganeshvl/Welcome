package com.entradahealth.entrada.core.inbox.exceptions;

public class QBException extends RuntimeException{

	public QBException(String msg){
		super(msg);
	}
	
	public QBException(String msg, Throwable throwable) {
		super(msg, throwable);
	}

}
