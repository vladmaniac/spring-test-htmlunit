/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.springframework.test.web.servlet;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.htmlunit.mail.data.Message;
import org.springframework.test.web.servlet.htmlunit.mail.data.MessageRepository;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Calendar;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.xpath;

/**
 * @author Rob Winch
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:mock-spring-data.xml",
		"file:src/main/webapp/WEB-INF/message-servlet.xml" })
@WebAppConfiguration
public class MockMvcCreateMessageTest {

	@Autowired
	private WebApplicationContext context;
	@Autowired
	private MessageRepository messages;

	private MockMvc mockMvc;

	@Before
	public void setup() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();

		Message message = getExpectedMessage();
		when(messages.save(any(Message.class))).thenAnswer(new Answer<Message>() {
			@Override
			public Message answer(InvocationOnMock invocation) throws Throwable {
				Object[] args = invocation.getArguments();
				Message result = (Message) args[0];
				result.setId(123L);
				return result;
			}
		});
		when(messages.findOne(anyLong())).thenReturn(message);
	}

	@After
	public void cleanup() {
		reset(messages);
	}

	@Test
	public void createMessage() throws Exception {
		MockHttpServletRequestBuilder createMessage = post("/messages/")
			.param("summary", "Spring Rocks")
			.param("text", "In case you didn't know, Spring Rocks!");

		mockMvc.perform(createMessage)
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/messages/123"));
	}

	@Test
	public void createMessageForm() throws Exception {
		mockMvc.perform(get("/messages/form"))
			.andExpect(xpath("//input[@name='summary']").exists())
			.andExpect(xpath("//textarea[@name='text']").exists());
	}

	@Test
	public void createMessageFormSubmit() throws Exception {
		String summaryParamName = "summary";
		String textParamName = "text";
		mockMvc.perform(get("/messages/form"))
				.andExpect(xpath("//input[@name='" + summaryParamName + "']").exists())
				.andExpect(xpath("//textarea[@name='" + textParamName + "']").exists());

		MockHttpServletRequestBuilder createMessage = post("/messages/")
				.param(summaryParamName, "Spring Rocks")
				.param(textParamName, "In case you didn't know, Spring Rocks!");

		mockMvc.perform(createMessage)
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/messages/123"));
	}

	private Message getExpectedMessage() {
		Message message = new Message();
		message.setCreated(Calendar.getInstance());
		message.setId(123L);
		message.setSummary("Summary");
		message.setText("Detailed message that you can see");
		return message;
	}
}
