/*
 * #%L
 * BroadleafCommerce Open Admin Platform
 * %%
 * Copyright (C) 2009 - 2013 Broadleaf Commerce
 * %%
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
 * #L%
 */
package com.mycompany.core.web.compatibility;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.broadleafcommerce.profile.core.domain.Customer;
import org.broadleafcommerce.profile.core.domain.CustomerAttribute;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * @author Jeff Fischer
 */
public class MediDocCompatibilityInterceptor extends HandlerInterceptorAdapter {

    @Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    	
		// TODO Auto-generated method stub
		boolean result = true;
		Customer customer = (Customer) request.getAttribute("customer");

		if (customer != null) {
			Map<String, CustomerAttribute> customerAttributes = customer.getCustomerAttributes();
			if ((customerAttributes.size() == 0 || customerAttributes.get("Authority").getValue().equals("User")) && request.getRequestURI().contains("doctor")) {
				response.sendRedirect("/mycompany");
				result = false;
			} else if (customerAttributes.size() != 0 && customerAttributes.get("Authority").getValue().equals("Doctor") && !request.getRequestURI().contains("doctor")) {
				response.sendRedirect("/mycompany/doctor");
				result = false;
			}
		}

		return result;
	}

}
