package com.suryans.calculator.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.suryans.calculator.exception.DivisionByZeroException;
import com.suryans.calculator.exception.NonFiniteOperandException;
import com.suryans.calculator.exception.ResultOverflowException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class CalculatorServiceTest {

	private final CalculatorService calculatorService = new CalculatorService();

	@Test
	void addReturnsSum() {
		assertThat(calculatorService.add(2, 3)).isEqualTo(5.0);
	}

	@Test
	void subtractReturnsDifference() {
		assertThat(calculatorService.subtract(2, 3)).isEqualTo(-1.0);
	}

	@Test
	void multiplyReturnsProduct() {
		assertThat(calculatorService.multiply(2.5, 4)).isEqualTo(10.0);
	}

	@Test
	void divideReturnsQuotient() {
		assertThat(calculatorService.divide(7, 2)).isEqualTo(3.5);
	}

	@Test
	void divideByZeroThrows() {
		assertThatThrownBy(() -> calculatorService.divide(1, 0)).isInstanceOf(DivisionByZeroException.class)
			.hasMessage("Division by zero is not allowed");
	}

	@ParameterizedTest
	@ValueSource(doubles = { Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY })
	void nonFiniteFirstOperandThrows(double value) {
		assertThatThrownBy(() -> calculatorService.add(value, 1)).isInstanceOf(NonFiniteOperandException.class)
			.hasMessage("Parameter 'a' must be a finite number, but was '" + value + "'");
	}

	@ParameterizedTest
	@ValueSource(doubles = { Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY })
	void nonFiniteSecondOperandThrows(double value) {
		assertThatThrownBy(() -> calculatorService.divide(1, value)).isInstanceOf(NonFiniteOperandException.class)
			.hasMessage("Parameter 'b' must be a finite number, but was '" + value + "'");
	}

	@Test
	void multiplyOverflowThrows() {
		assertThatThrownBy(() -> calculatorService.multiply(Double.MAX_VALUE, 2))
			.isInstanceOf(ResultOverflowException.class)
			.hasMessage("Result of multiply is too large to represent as a finite number");
	}

}
