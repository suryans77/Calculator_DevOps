package com.suryans.calculator.exception;

public class NonFiniteOperandException extends CalculationException {

	public NonFiniteOperandException(String name, double value) {
		super("Parameter '" + name + "' must be a finite number, but was '" + value + "'");
	}

}
