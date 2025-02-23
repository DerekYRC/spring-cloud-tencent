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

package com.tencent.cloud.plugin.featureenv;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.tencent.cloud.common.constant.RouterConstant;
import com.tencent.cloud.polaris.router.PolarisRouterContext;
import com.tencent.polaris.api.pojo.DefaultServiceInstances;
import com.tencent.polaris.api.pojo.ServiceInstances;
import com.tencent.polaris.api.pojo.ServiceKey;
import com.tencent.polaris.plugins.router.metadata.MetadataRouter;
import com.tencent.polaris.router.api.rpc.ProcessRoutersRequest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Test for {@link FeatureEnvRouterRequestInterceptor}.
 * @author lepdou 2022-07-12
 */
@RunWith(MockitoJUnitRunner.class)
public class FeatureEnvRouterRequestInterceptorTest {

	@Test
	public void testDefaultRouterKey() {
		Map<String, String> labels = new HashMap<>();
		labels.put("featureenv", "blue");
		PolarisRouterContext routerContext = new PolarisRouterContext();
		routerContext.putLabels(RouterConstant.ROUTER_LABELS, labels);

		ProcessRoutersRequest request = new ProcessRoutersRequest();
		ServiceInstances serviceInstances = new DefaultServiceInstances(Mockito.mock(ServiceKey.class), new ArrayList<>());
		request.setDstInstances(serviceInstances);

		FeatureEnvRouterRequestInterceptor interceptor = new FeatureEnvRouterRequestInterceptor();

		interceptor.apply(request, routerContext);

		Map<String, String> metadataRouterLabels = request.getRouterMetadata().get(MetadataRouter.ROUTER_TYPE_METADATA);
		Assert.assertEquals(1, metadataRouterLabels.size());
		Assert.assertEquals("blue", metadataRouterLabels.get("featureenv"));
	}

	@Test
	public void testSpecifyRouterKey() {
		Map<String, String> labels = new HashMap<>();
		labels.put("system-feature-env-router-label", "specify-env");
		labels.put("specify-env", "blue");
		PolarisRouterContext routerContext = new PolarisRouterContext();
		routerContext.putLabels(RouterConstant.ROUTER_LABELS, labels);

		ProcessRoutersRequest request = new ProcessRoutersRequest();
		ServiceInstances serviceInstances = new DefaultServiceInstances(Mockito.mock(ServiceKey.class), new ArrayList<>());
		request.setDstInstances(serviceInstances);

		FeatureEnvRouterRequestInterceptor interceptor = new FeatureEnvRouterRequestInterceptor();

		interceptor.apply(request, routerContext);

		Map<String, String> metadataRouterLabels = request.getRouterMetadata().get(MetadataRouter.ROUTER_TYPE_METADATA);
		Assert.assertEquals(1, metadataRouterLabels.size());
		Assert.assertEquals("blue", metadataRouterLabels.get("specify-env"));
	}

	@Test
	public void testNotExistedEnvLabel() {
		Map<String, String> labels = new HashMap<>();
		labels.put("system-feature-env-router-label", "specify-env");
		PolarisRouterContext routerContext = new PolarisRouterContext();
		routerContext.putLabels(RouterConstant.ROUTER_LABELS, labels);

		ProcessRoutersRequest request = new ProcessRoutersRequest();
		ServiceInstances serviceInstances = new DefaultServiceInstances(Mockito.mock(ServiceKey.class), new ArrayList<>());
		request.setDstInstances(serviceInstances);

		FeatureEnvRouterRequestInterceptor interceptor = new FeatureEnvRouterRequestInterceptor();

		interceptor.apply(request, routerContext);

		Map<String, String> metadataRouterLabels = request.getRouterMetadata().get(MetadataRouter.ROUTER_TYPE_METADATA);
		Assert.assertEquals(1, metadataRouterLabels.size());
		Assert.assertEquals("NOT_EXISTED_ENV", metadataRouterLabels.get("specify-env"));
	}
}
