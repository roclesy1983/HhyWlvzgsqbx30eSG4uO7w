package com.mycompany.web.email;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;
import javax.mail.internet.MimeMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.common.email.domain.EmailTargetImpl;
import org.broadleafcommerce.common.email.service.EmailService;
import org.broadleafcommerce.common.email.service.info.EmailInfo;
import org.broadleafcommerce.core.catalog.service.CatalogService;
import org.broadleafcommerce.core.order.domain.DiscreteOrderItem;
import org.broadleafcommerce.core.order.domain.Order;
import org.broadleafcommerce.core.order.service.OrderService;
import org.broadleafcommerce.profile.core.domain.Customer;
import org.springframework.beans.factory.annotation.Value;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.Translate.TranslateOption;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import com.mycompany.worklow.checkout.SendAppointmentConfirmationEmailToDoctorActivity;

public class TransferApptEmail {

	protected static final Log LOG = LogFactory.getLog(SendAppointmentConfirmationEmailToDoctorActivity.class);

	@Value("${site.emailAddress}")
	protected String fromEmailAddress;

	@Resource(name = "blEmailService")
	protected EmailService emailService;

	@Resource(name = "blCatalogService")
	protected CatalogService catalogService;

	@Resource(name = "blOrderService")
	protected OrderService orderService;

	public void translateAndSendEmail(MimeMessage mimeMessage) throws Exception {

		Pattern p = Pattern.compile("[0-9]+");
		Matcher m = p.matcher(mimeMessage.getSubject());
		String orderNumber = "";
		while (m.find()) {
			orderNumber = m.group();
		}
		Order order = orderService.findOrderByOrderNumber(orderNumber);

		Customer patientContact = order.getCustomer();
		DiscreteOrderItem discreteOrderItem = orderService.findDiscreteOrderItemByOrderNumber(orderNumber);
		Customer clinicContact = catalogService.readCustomerByProductId(discreteOrderItem.getProduct().getId());
		String clinicEmailAddress = clinicContact.getEmailAddress();
		String patientEmailAddress = patientContact.getEmailAddress();

		HashMap<String, Object> vars = new HashMap<String, Object>();

		EmailInfo emailInfo = new EmailInfo();

		emailInfo.setFromAddress(fromEmailAddress);
		emailInfo.setSubject(mimeMessage.getSubject());
		EmailTargetImpl emailTarget = new EmailTargetImpl();

		String translateMsg = mimeMessage.getContent().toString().split("[-]{15,}")[0].replaceAll("(\r\n|\n)", "<br />");
		if (mimeMessage.getFrom()[0].toString().contains(patientEmailAddress)) {
			translateMsg = "-------------------------<br />[---患者様と連絡がある場合、このメールに返信してください。分かりやすい日本語を書いてください。---]<br />" + translate(translateMsg, "en") + "<br />-------------------------";
			emailInfo.setMessageBody(translateMsg);
			vars.put("message", translateMsg);
			emailTarget.setEmailAddress(clinicEmailAddress);
		} else if (mimeMessage.getFrom()[0].toString().contains(clinicEmailAddress)) {
			translateMsg = "-------------------------<br />[---When contacting the doctor, please reply this email. Easy understanding English please.---]<br />" + translate(translateMsg, "ja") + "<br />-------------------------";
			emailInfo.setMessageBody(translateMsg);
			vars.put("message", translateMsg);
			emailTarget.setEmailAddress(patientEmailAddress);
		}

		// Email service failing should not trigger rollback
		try {
			emailService.sendBasicEmail(emailInfo, emailTarget, vars);
		} catch (Exception e) {
			LOG.error(e);
		}

	}

	public String translate(String translateText, String sourceLanguage) {
		Translate translate = TranslateOptions.newBuilder().setApiKey("AIzaSyDq18Z3RIAR_PKn0dLmsYwUkbRjRZKu4Bs").build().getService();
		String result = translateText;
		Translation translation = null;
		if ("ja".equals(sourceLanguage)) {
			translation = translate.translate(translateText, TranslateOption.sourceLanguage("ja"), TranslateOption.targetLanguage("en"));
		} else if ("en".equals(sourceLanguage)) {
			translation = translate.translate(translateText, TranslateOption.sourceLanguage("en"), TranslateOption.targetLanguage("ja"));
		}
		if (translation != null) {
			result = translation.getTranslatedText();
		}
		return result;
	}

}
