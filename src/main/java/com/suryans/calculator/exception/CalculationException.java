package com.suryans.calculator.exception;

public abstract class CalculationException extends RuntimeException {

	protected CalculationException(String message) {
		super(message);
	}

}
