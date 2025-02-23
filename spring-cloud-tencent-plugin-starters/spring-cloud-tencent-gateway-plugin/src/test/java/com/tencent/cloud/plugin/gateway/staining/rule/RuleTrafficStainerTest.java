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

package com.tencent.cloud.plugin.gateway.staining.rule;

import java.util.Map;

import com.tencent.polaris.configuration.api.core.ConfigFile;
import com.tencent.polaris.configuration.api.core.ConfigFileService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.util.CollectionUtils;

import static org.mockito.Mockito.when;

/**
 * * Test for {@link RuleTrafficStainer}.
 *
 * @author derek.yi 2022-11-03
 */
@RunWith(MockitoJUnitRunner.class)
public class RuleTrafficStainerTest {

	@Mock
	private ConfigFileService configFileService;

	private final String testNamespace = "testNamespace";
	private final String testGroup = "testGroup";
	private final String testFileName = "rule.json";

	@Test
	public void testNoStainingRule() {
		RuleStainingProperties ruleStainingProperties = new RuleStainingProperties();
		ruleStainingProperties.setNamespace(testNamespace);
		ruleStainingProperties.setGroup(testGroup);
		ruleStainingProperties.setFileName(testFileName);

		ConfigFile configFile = Mockito.mock(ConfigFile.class);
		when(configFile.getContent()).thenReturn("");
		when(configFileService.getConfigFile(testNamespace, testGroup, testFileName)).thenReturn(configFile);

		StainingRuleManager stainingRuleManager = new StainingRuleManager(ruleStainingProperties, configFileService);
		RuleStainingExecutor ruleStainingExecutor = new RuleStainingExecutor();
		RuleTrafficStainer ruleTrafficStainer = new RuleTrafficStainer(stainingRuleManager, ruleStainingExecutor);
		Map<String, String> map = ruleTrafficStainer.apply(null);
		Assert.assertTrue(CollectionUtils.isEmpty(map));
	}

	@Test
	public void testWithStainingRule() {
		RuleStainingProperties ruleStainingProperties = new RuleStainingProperties();
		ruleStainingProperties.setNamespace(testNamespace);
		ruleStainingProperties.setGroup(testGroup);
		ruleStainingProperties.setFileName(testFileName);

		ConfigFile configFile = Mockito.mock(ConfigFile.class);
		when(configFile.getContent()).thenReturn("{\n"
				+ "    \"rules\":[\n"
				+ "        {\n"
				+ "            \"conditions\":[\n"
				+ "                {\n"
				+ "                    \"key\":\"${http.query.uid}\",\n"
				+ "                    \"values\":[\"1000\"],\n"
				+ "                    \"operation\":\"EQUALS\"\n"
				+ "                }\n"
				+ "            ],\n"
				+ "            \"labels\":[\n"
				+ "                {\n"
				+ "                    \"key\":\"env\",\n"
				+ "                    \"value\":\"blue\"\n"
				+ "                }\n"
				+ "            ]\n"
				+ "        }\n"
				+ "    ]\n"
				+ "}");
		when(configFileService.getConfigFile(testNamespace, testGroup, testFileName)).thenReturn(configFile);

		StainingRuleManager stainingRuleManager = new StainingRuleManager(ruleStainingProperties, configFileService);
		RuleStainingExecutor ruleStainingExecutor = new RuleStainingExecutor();
		RuleTrafficStainer ruleTrafficStainer = new RuleTrafficStainer(stainingRuleManager, ruleStainingExecutor);

		MockServerHttpRequest request = MockServerHttpRequest.get("/users")
				.queryParam("uid", "1000").build();
		MockServerWebExchange exchange = new MockServerWebExchange.Builder(request).build();

		Map<String, String> map = ruleTrafficStainer.apply(exchange);
		Assert.assertNotNull(map);
		Assert.assertEquals(1, map.size());
		Assert.assertEquals("blue", map.get("env"));
	}
}
