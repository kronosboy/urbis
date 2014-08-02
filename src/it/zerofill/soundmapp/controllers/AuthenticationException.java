package it.zerofill.soundmapp.controllers;

public class AuthenticationException extends Exception{

	private static final long serialVersionUID = -6586605238199510687L;
	private int errorCode;

	public int getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

	public AuthenticationException(int errorCode){
		this.errorCode = errorCode;
	}
}
