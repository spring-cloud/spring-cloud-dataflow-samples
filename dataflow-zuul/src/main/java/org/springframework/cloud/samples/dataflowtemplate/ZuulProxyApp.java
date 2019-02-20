/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.cloud.samples.dataflowtemplate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.stereotype.Component;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;

/**
 *
 * @author Gunnar Hillert
 *
 */
@SpringBootApplication
@EnableZuulProxy
public class ZuulProxyApp  {

	public static void main(String[] args) {
		SpringApplication.run(ZuulProxyApp.class, args);
	}

	private final Logger logger = LoggerFactory.getLogger(ZuulProxyApp.class);

	@Component
	public class CustomZuulFilter extends ZuulFilter {

		@Override
		public Object run() {
			RequestContext ctx = RequestContext.getCurrentContext();
			logger.info("RequestURL: " + ctx.getRequest().getRequestURL());
			return null;
		}

		@Override
		public boolean shouldFilter() {
		   return true;
		}

		@Override
		public String filterType() {
			return "pre";
		}

		@Override
		public int filterOrder() {
			return 0;
		}
	}

}
