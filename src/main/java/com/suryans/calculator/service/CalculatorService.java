package com.suryans.calculator.service;

import com.suryans.calculator.exception.DivisionByZeroException;
import com.suryans.calculator.exception.NonFiniteOperandException;
import com.suryans.calculator.exception.ResultOverflowException;
import org.springframework.stereotype.Service;

@Service
public class CalculatorService {

	public double add(double a, double b) {
		requireFiniteOperands(a, b);
		return requireFiniteResult("add", a + b);
	}

	public double subtract(double a, double b) {
		requireFiniteOperands(a, b);
		return requireFiniteResult("subtract", a - b);
	}

	public double multiply(double a, double b) {
		requireFiniteOperands(a, b);
		return requireFiniteResult("multiply", a * b);
	}

	public double divide(double a, double b) {
		requireFiniteOperands(a, b);
		if (b == 0) {
			throw new DivisionByZeroException();
		}
		return requireFiniteResult("divide", a / b);
	}

	private void requireFiniteOperands(double a, double b) {
		if (!Double.isFinite(a)) {
			throw new NonFiniteOperandException("a", a);
		}
		if (!Double.isFinite(b)) {
			throw new NonFiniteOperandException("b", b);
		}
	}

	private double requireFiniteResult(String operation, double result) {
		if (!Double.isFinite(result)) {
			throw new ResultOverflowException(operation);
		}
		return result;
	}

}
