/*
 * Copyright 2008-2013 the original author or authors.
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

package com.mycompany.worklow.checkout;

import java.util.HashMap;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.common.email.domain.EmailTargetImpl;
import org.broadleafcommerce.common.email.service.EmailService;
import org.broadleafcommerce.common.email.service.info.EmailInfo;
import org.broadleafcommerce.core.catalog.service.CatalogService;
import org.broadleafcommerce.core.checkout.service.workflow.CheckoutSeed;
import org.broadleafcommerce.core.order.domain.Order;
import org.broadleafcommerce.core.workflow.BaseActivity;
import org.broadleafcommerce.core.workflow.ProcessContext;
import org.broadleafcommerce.profile.core.domain.Customer;
import org.springframework.beans.factory.annotation.Value;

/**
 * Send order confirmation email
 *
 * @author Phillip Verheyden (phillipuniverse)
 * @author Joshua Skorton (jskorton)
 */
public class SendAppointmentConfirmationEmailToDoctorActivity extends BaseActivity<ProcessContext<CheckoutSeed>> {

	protected static final Log LOG = LogFactory.getLog(SendAppointmentConfirmationEmailToDoctorActivity.class);

	@Value("${site.emailAddress}")
	protected String fromEmailAddress;

	@Resource(name = "blEmailService")
	protected EmailService emailService;

	@Resource(name = "blCatalogService")
	protected CatalogService catalogService;

	@Override
	public ProcessContext<CheckoutSeed> execute(ProcessContext<CheckoutSeed> context) throws Exception {
		Order order = context.getSeedData().getOrder();
		HashMap<String, Object> vars = new HashMap<String, Object>();
		vars.put("customer", order.getCustomer());
		vars.put("orderNumber", order.getOrderNumber());
		vars.put("order", order);
		Customer clinic = catalogService.readCustomerByProductId(order.getDiscreteOrderItems().get(0).getProduct().getId());
		String emailAddressTo = clinic.getEmailAddress();
		EmailInfo emailInfo = new EmailInfo();

		emailInfo.setFromAddress(fromEmailAddress);
		emailInfo.setSubject("[Appointment Number:" + order.getOrderNumber() + "]");
		emailInfo.setMessageBody("[-------------------------]<br />新しい予約はきました。<br />患者様：" + order.getCustomer().getLastName() + " " + order.getCustomer().getFirstName() + "<br />予約希望日："
				+ order.getOrderItems().get(0).getOrderItemAttributes() + "<br />変更がある場合、このメールに返信して患者様に連絡してください<br />よろしくお願いします。");
		EmailTargetImpl emailTarget = new EmailTargetImpl();
		emailTarget.setEmailAddress(emailAddressTo);

		// Email service failing should not trigger rollback
		try {
			emailService.sendBasicEmail(emailInfo, emailTarget, vars);
		} catch (Exception e) {
			LOG.error(e);
		}
		return context;
	}

}
