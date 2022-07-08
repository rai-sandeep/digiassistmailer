package com.digiassist.mailer;

import java.util.logging.Logger;

import com.digiassist.mailer.domain.Email;
import com.mailjet.client.ClientOptions;
import com.mailjet.client.MailjetClient;
import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.transactional.SendContact;
import com.mailjet.client.transactional.SendEmailsRequest;
import com.mailjet.client.transactional.TransactionalEmail;
import com.mailjet.client.transactional.response.SendEmailsResponse;

public enum MailJetMailer implements Mailer {
	INSTANCE; //use enum to create singleton
	
	private static final Logger logger = Logger.getLogger(MailJetMailer.class.getName());
	
	private final MailjetClient client;
	
	private MailJetMailer() {
        ClientOptions options = ClientOptions.builder()
                .apiKey(System.getenv("mailgun-apikey"))
                .apiSecretKey(System.getenv("mailgun-apisecret"))
                .build();

        client = new MailjetClient(options);
	}

	@Override
	public void sendEmail(Email email) throws MailjetException {		
		
        TransactionalEmail message = TransactionalEmail
                .builder()
                .to(new SendContact(email.getToEmail() , email.getToName()))
                .from(new SendContact(email.getFromEmail(), email.getFromName()))
                .htmlPart(email.getBody())
                .subject(email.getSubject())
                .build();

        SendEmailsRequest request = SendEmailsRequest
                .builder()
                .message(message) 
                .build();

        SendEmailsResponse response = request.sendWith(client);
        logger.info("SendEmailsResponse: "+response.toString());		
	}

}
