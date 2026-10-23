package com.suryans.calculator.exception;

public class DivisionByZeroException extends CalculationException {

	public DivisionByZeroException() {
		super("Division by zero is not allowed");
	}

}
