/*
 * Copyright 2008-2012 the original author or authors.
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

package com.mycompany.controller.account;

import org.broadleafcommerce.common.exception.ServiceException;
import org.broadleafcommerce.core.order.domain.Order;
import org.broadleafcommerce.core.order.service.type.OrderStatus;
import org.broadleafcommerce.core.web.controller.account.BroadleafOrderHistoryController;
import org.broadleafcommerce.core.web.controller.account.BroadleafUpdateAccountController;
import org.broadleafcommerce.core.web.controller.account.UpdateAccountForm;
import org.broadleafcommerce.profile.core.domain.Customer;
import org.broadleafcommerce.profile.core.service.CountryService;
import org.broadleafcommerce.profile.core.service.StateService;
import org.broadleafcommerce.profile.web.core.CustomerState;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/doctor")
public class DoctorController extends BroadleafOrderHistoryController {

    @Resource(name = "blStateService")
    StateService stateService;

    @Resource(name = "blCountryService")
    CountryService countryService;

    @RequestMapping(method = RequestMethod.GET)
    public String viewOrderHistory(HttpServletRequest request, Model model) {
    	Customer customer = (Customer)request.getAttribute("customer");
    	String emailAddress = customer.getEmailAddress();
    	Long productId = catalogService.findProductAttributeByValue(emailAddress).getProduct().getId();
        List<Order> orders = orderService.findOrdersByProductId(productId);
        model.addAttribute("orders", orders);
        return "/account/doctor";
    }

    @RequestMapping(value = "/{orderNumber}", method = RequestMethod.GET)
    public String viewOrderDetails(HttpServletRequest request, Model model, @PathVariable("orderNumber") String orderNumber) {
        Order order = orderService.findOrderByOrderNumber(orderNumber);
        if (order == null) {
            throw new IllegalArgumentException("The orderNumber provided is not valid");
        }
        model.addAttribute("order", order);
        return isAjaxRequest(request) ? getOrderDetailsView() : getOrderDetailsRedirectView();
    }
    
    @RequestMapping(value = "/complete/{orderNumber}", method = RequestMethod.GET)
    public String CompleteOrder(HttpServletRequest request, Model model, @PathVariable("orderNumber") String orderNumber) {
    	Order order = orderService.findOrderByOrderNumber(orderNumber);
        Order orderModel = orderService.completeOrder(order);
        if (orderModel == null) {
            throw new IllegalArgumentException("The orderNumber provided is not valid");
        }
        return "redirect:/doctor";
    }
    
    @RequestMapping(value = "/cancel/{orderNumber}", method = RequestMethod.GET)
    public String CancelOrder(HttpServletRequest request, Model model, @PathVariable("orderNumber") String orderNumber) {
    	Order order = orderService.findOrderByOrderNumber(orderNumber);
        Order orderModel = orderService.cancelOrderByDoctor(order);
        if (orderModel == null) {
            throw new IllegalArgumentException("The orderNumber provided is not valid");
        }
        return "redirect:/doctor";
    }

}
