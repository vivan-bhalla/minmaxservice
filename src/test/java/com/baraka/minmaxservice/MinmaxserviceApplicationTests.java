package com.baraka.minmaxservice;

import com.jayway.jsonpath.internal.function.numeric.Min;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.net.URI;
import java.net.URISyntaxException;

@SpringBootTest
class MinmaxserviceApplicationTests {

	private String HOME_PAGE_URL_TEXT = "Welcome! Please use loaclhost:8080/getprice/{stock-name} to get the price of the stock of your choice!";

	private MockMvc mockMvc;
	@Autowired
	private WebApplicationContext webApplicationContext;

	@BeforeEach
	public void setup() {
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
	}

	@Test
	public void homePageURL() throws Exception {
		mockMvc.perform(get("/")).andExpect(status().isOk())
				.andExpect(content().string(HOME_PAGE_URL_TEXT));
	}
	@Test
	void contextLoads() {
	}

}
