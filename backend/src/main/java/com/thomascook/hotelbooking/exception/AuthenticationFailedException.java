package com.thomascook.hotelbooking.exception;
public class AuthenticationFailedException extends RuntimeException { public AuthenticationFailedException() { super("Invalid email or password"); } }
