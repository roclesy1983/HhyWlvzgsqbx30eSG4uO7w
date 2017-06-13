package com.mycompany.core.web.email;

import javax.mail.internet.MimeMessage;

import org.broadleafcommerce.common.time.SystemTime;
import org.broadleafcommerce.core.catalog.domain.Product;
import org.broadleafcommerce.core.catalog.domain.ProductImpl;
import org.broadleafcommerce.profile.core.domain.Customer;
import org.broadleafcommerce.profile.core.domain.CustomerImpl;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.Translate.TranslateOption;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;

public class TransferApptEmail {

	public void translateAndSendEmail(MimeMessage mimeMessage) throws Exception {
		mimeMessage.getFrom();
		mimeMessage.getReplyTo();
		mimeMessage.getSubject();

		Customer patient = new CustomerImpl();
		Product clinic = new ProductImpl();

		Translate translate = TranslateOptions.newBuilder().setApiKey("AIzaSyDq18Z3RIAR_PKn0dLmsYwUkbRjRZKu4Bs").build().getService();
		if ("ja".equals(translate.detect(mimeMessage.getContent().toString()).getLanguage())) {
			Translation translation = translate.translate(mimeMessage.getSubject() + "----------" + mimeMessage.getContent().toString(), TranslateOption.sourceLanguage("ja"),
					TranslateOption.targetLanguage("en"));
			System.out.println(translation.getTranslatedText() + "----------" + SystemTime.asDate());
		} else {
			Translation translation = translate.translate(mimeMessage.getSubject() + "----------" + mimeMessage.getContent().toString(), TranslateOption.sourceLanguage("en"),
					TranslateOption.targetLanguage("ja"));
			System.out.println(translation.getTranslatedText() + "----------" + SystemTime.asDate());
		}

	}
}
