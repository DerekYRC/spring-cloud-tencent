/*
 * Tencent is pleased to support the open source community by making Spring Cloud Tencent available.
 *
 * Copyright (C) 2019 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * Licensed under the BSD 3-Clause License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 */

package com.tencent.cloud.metadata.core;

import java.util.Map;

import com.tencent.cloud.common.metadata.MetadataContext;
import com.tencent.cloud.common.metadata.MetadataContextHolder;
import com.tencent.cloud.common.util.JacksonUtils;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * Test for {@link TransHeadersTransfer}.
 *
 * @author lingxiao.wlx
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT,
		classes = DecodeTransferMetadataServletFilterTest.TestApplication.class,
		properties = {"spring.config.location = classpath:application-test.yml", "spring.main.web-application-type=reactive"})
public class TransHeadersTransferTest {
	@AfterClass
	public static void afterClass() {
		MetadataContextHolder.remove();
	}

	@Test
	public void transferServletTest() {
		MetadataContext metadataContext = MetadataContextHolder.get();
		metadataContext.setTransHeaders("header1,header2,header3", "");
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader("header1", "1");
		request.addHeader("header2", "2");
		request.addHeader("header3", "3");
		TransHeadersTransfer.transfer(request);
		Map<String, String> transHeadersKV = MetadataContextHolder.get().getTransHeadersKV();
		Assertions.assertEquals(transHeadersKV.get("header1"), "1");
		Assertions.assertEquals(transHeadersKV.get("header2"), "2");
		Assertions.assertEquals(transHeadersKV.get("header3"), "3");
	}

	@Test
	public void transferReactiveTest() {
		MetadataContext metadataContext = MetadataContextHolder.get();
		metadataContext.setTransHeaders("header1,header2,header3", "");
		MockServerHttpRequest.BaseBuilder<?> builder = MockServerHttpRequest.get("");
		String[] header1 = {"1"};
		String[] header2 = {"2"};
		String[] header3 = {"3"};
		builder.header("header1", header1);
		builder.header("header2", header2);
		builder.header("header3", header3);
		MockServerHttpRequest request = builder.build();
		TransHeadersTransfer.transfer(request);
		Map<String, String> transHeadersKV = MetadataContextHolder.get().getTransHeadersKV();
		Assertions.assertEquals(transHeadersKV.get("header1"), JacksonUtils.serialize2Json(header1));
		Assertions.assertEquals(transHeadersKV.get("header2"), JacksonUtils.serialize2Json(header2));
		Assertions.assertEquals(transHeadersKV.get("header3"), JacksonUtils.serialize2Json(header3));
	}
}
