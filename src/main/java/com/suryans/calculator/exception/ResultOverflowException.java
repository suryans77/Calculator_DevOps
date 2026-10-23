package com.suryans.calculator.exception;

public class ResultOverflowException extends CalculationException {

	public ResultOverflowException(String operation) {
		super("Result of " + operation + " is too large to represent as a finite number");
	}

}
