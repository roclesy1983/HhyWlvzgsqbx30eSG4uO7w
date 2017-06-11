package com.mycompany.core.web.email;

import javax.mail.internet.MimeMessage;

import org.broadleafcommerce.common.time.SystemTime;

public class TransferApptEmail {

	public void translateAndSendEmail(MimeMessage mimeMessage) throws Exception {
		mimeMessage.getFrom();
		mimeMessage.getReplyTo();
		mimeMessage.getSubject();
		System.out.println(mimeMessage.getContent().toString() + "----------" + SystemTime.asDate());
	}
}
