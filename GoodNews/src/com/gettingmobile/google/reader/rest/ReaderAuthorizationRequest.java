package com.gettingmobile.google.reader.rest;

import com.gettingmobile.google.rest.AuthorizationRequest;

public class ReaderAuthorizationRequest extends AuthorizationRequest {
	public static final String SERVICE_NAME = "reader";

	public ReaderAuthorizationRequest(String email, String password) {
		super(email, password, SERVICE_NAME);
	}

}
