package com.mycompany.web.email;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;
import javax.mail.internet.MimeMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.common.email.service.EmailService;
import org.broadleafcommerce.common.email.service.info.EmailInfo;
import org.broadleafcommerce.common.time.SystemTime;
import org.broadleafcommerce.core.catalog.service.CatalogService;
import org.broadleafcommerce.core.order.domain.DiscreteOrderItem;
import org.broadleafcommerce.core.order.domain.Order;
import org.broadleafcommerce.core.order.service.OrderService;
import org.broadleafcommerce.profile.core.domain.Customer;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.Translate.TranslateOption;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import com.mycompany.worklow.checkout.SendAppointmentConfirmationEmailToDoctorActivity;

public class TransferApptEmail {

	protected static final Log LOG = LogFactory.getLog(SendAppointmentConfirmationEmailToDoctorActivity.class);

	@Resource(name = "blEmailService")
	protected EmailService emailService;

	@Resource(name = "blCatalogService")
	protected CatalogService catalogService;

	@Resource(name = "blOrderService")
	protected OrderService orderService;

	@Resource(name = "blApptPatDocConfirmationEmailInfo")
	protected EmailInfo apptPatDocConfirmationEmailInfo;

	public void translateAndSendEmail(MimeMessage mimeMessage) throws Exception {
		mimeMessage.getFrom();
		mimeMessage.getReplyTo();
		mimeMessage.getSubject();

		System.out.println(mimeMessage.getSubject() + mimeMessage.getContent() + "----------" + SystemTime.asDate());

		String translateMsg = mimeMessage.getContent().toString();
		System.out.println(translateMsg + "----------" + SystemTime.asDate());
		Pattern p = Pattern.compile("[0-9]+");
		Matcher m = p.matcher(mimeMessage.getSubject());
		String orderNumber = "";
		while (m.find()) {
			orderNumber = m.group();
		}
		Order order = orderService.findOrderByOrderNumber(orderNumber);

		HashMap<String, Object> vars = new HashMap<String, Object>();
		vars.put("message", translateMsg);
		Customer patientContact = order.getCustomer();
		DiscreteOrderItem discreteOrderItem = orderService.findDiscreteOrderItemByOrderNumber(orderNumber);
		Customer clinicContact = catalogService.readCustomerByProductId(discreteOrderItem.getProduct().getId());
		String clinicEmailAddress = clinicContact.getEmailAddress();
		String patientEmailAddress = patientContact.getEmailAddress();

		// Email service failing should not trigger rollback
		try {
			apptPatDocConfirmationEmailInfo.setSubject(mimeMessage.getSubject());
			if (mimeMessage.getFrom().equals(patientEmailAddress)) {
				emailService.sendTemplateEmail(clinicEmailAddress, getApptPatDocConfirmationEmailInfo(), vars);
			} else if (mimeMessage.getFrom().equals(clinicEmailAddress)) {
				emailService.sendTemplateEmail(patientEmailAddress, getApptPatDocConfirmationEmailInfo(), vars);
			}
		} catch (Exception e) {
			LOG.error(e);
		}

	}

	public String translate(String translateText) {
		Translate translate = TranslateOptions.newBuilder().setApiKey("AIzaSyDq18Z3RIAR_PKn0dLmsYwUkbRjRZKu4Bs").build().getService();
		Translation translation;
		if ("ja".equals(translate.detect(translateText).getLanguage())) {
			translation = translate.translate(translateText, TranslateOption.sourceLanguage("ja"), TranslateOption.targetLanguage("en"));
		} else {
			translation = translate.translate(translateText, TranslateOption.sourceLanguage("en"), TranslateOption.targetLanguage("ja"));
		}
		return translation.getTranslatedText();
	}

	public EmailInfo getApptPatDocConfirmationEmailInfo() {
		return apptPatDocConfirmationEmailInfo;
	}

	public void setApptPatDocConfirmationEmailInfo(EmailInfo apptPatDocConfirmationEmailInfo) {
		this.apptPatDocConfirmationEmailInfo = apptPatDocConfirmationEmailInfo;
	}

}
