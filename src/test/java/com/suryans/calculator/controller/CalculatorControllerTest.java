package com.suryans.calculator.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.suryans.calculator.service.CalculatorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(CalculatorController.class)
@Import(CalculatorService.class)
class CalculatorControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void addReturnsResult() throws Exception {
		expectResult(perform("/api/v1/add?a=2&b=3"), "add", 2, 3, 5);
	}

	@Test
	void subtractReturnsResult() throws Exception {
		expectResult(perform("/api/v1/subtract?a=2&b=3"), "subtract", 2, 3, -1);
	}

	@Test
	void multiplyReturnsResult() throws Exception {
		expectResult(perform("/api/v1/multiply?a=2.5&b=4"), "multiply", 2.5, 4, 10);
	}

	@Test
	void divideReturnsResult() throws Exception {
		expectResult(perform("/api/v1/divide?a=7&b=2"), "divide", 7, 2, 3.5);
	}

	@Test
	void divideByZeroReturnsBadRequest() throws Exception {
		expectBadRequest(perform("/api/v1/divide?a=1&b=0"), "/api/v1/divide", "Division by zero is not allowed");
	}

	@Test
	void missingParameterReturnsBadRequest() throws Exception {
		expectBadRequest(perform("/api/v1/add?a=1"), "/api/v1/add", "Missing required parameter 'b'");
	}

	@Test
	void nonNumericParameterReturnsBadRequest() throws Exception {
		expectBadRequest(perform("/api/v1/add?a=abc&b=1"), "/api/v1/add",
				"Parameter 'a' must be a number, but was 'abc'");
	}

	@Test
	void nanParameterReturnsBadRequest() throws Exception {
		expectBadRequest(perform("/api/v1/subtract?a=1&b=NaN"), "/api/v1/subtract",
				"Parameter 'b' must be a finite number, but was 'NaN'");
	}

	@Test
	void overflowReturnsBadRequest() throws Exception {
		expectBadRequest(perform("/api/v1/multiply?a=1e308&b=10"), "/api/v1/multiply",
				"Result of multiply is too large to represent as a finite number");
	}

	private ResultActions perform(String uri) throws Exception {
		return mockMvc.perform(get(uri));
	}

	private void expectResult(ResultActions result, String operation, double a, double b, double expected)
			throws Exception {
		result.andExpect(status().isOk())
			.andExpect(jsonPath("$.operation").value(operation))
			.andExpect(jsonPath("$.a").value(a))
			.andExpect(jsonPath("$.b").value(b))
			.andExpect(jsonPath("$.result").value(expected));
	}

	private void expectBadRequest(ResultActions result, String path, String message) throws Exception {
		result.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.timestamp").isNotEmpty())
			.andExpect(jsonPath("$.status").value(400))
			.andExpect(jsonPath("$.error").value("Bad Request"))
			.andExpect(jsonPath("$.message").value(message))
			.andExpect(jsonPath("$.path").value(path));
	}

}
