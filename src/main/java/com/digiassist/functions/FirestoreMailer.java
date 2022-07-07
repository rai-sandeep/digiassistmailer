package com.digiassist.functions;

import java.text.MessageFormat;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;

import com.google.cloud.functions.Context;
import com.google.cloud.functions.RawBackgroundFunction;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mailjet.client.ClientOptions;
import com.mailjet.client.MailjetClient;
import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.transactional.SendContact;
import com.mailjet.client.transactional.SendEmailsRequest;
import com.mailjet.client.transactional.TransactionalEmail;
import com.mailjet.client.transactional.response.SendEmailsResponse;

public class FirestoreMailer implements RawBackgroundFunction {
	private static final Logger logger = Logger.getLogger(FirestoreMailer.class.getName());
	
	private final String senderEmail = "gcpsandeeprai@gmail.com";
	private final String senderName = "Digi Assist";
	private final Gson gson = new Gson();
	
	private final String emailBodyPart1 = "<html>\n"
			+ "<body style=\"font-family: verdana, helvetica, sans-serif; font-size: 14px; color: #23364d\">\n"
			+ "Dear {0},\n"
			+ "<br>\n"
			+ "<br>\n"
			+ "<b>Greetings from Digi Assist!</b>\n"
			+ "<br>\n"
			+ "<br>\n"
			+ "Your ticket has been {1}. Please find your ticket information below.\n"
			+ "<br>\n"
			+ "<br>\n"
			+ "<table>\n"
			+ "<tr>\n"
			+ "<td><b>Ticket Id:</b></td><td>{2}</td>\n"
			+ "</tr>\n"
			+ "<tr>\n"
			+ "<td><b>Query: </b></td><td>{3}</td>\n"
			+ "</tr>\n";
	
	private final String emailBodyPart2 = "<tr>\n"
			+ "<td><b>Response: </b></td><td>{0}</td>\n"
			+ "</tr>\n";
	
	private final String emailBodyPart3 =  "</table>\n"
			+ "<br>\n"
			+ "Thank you for using Digi Assist!\n"
			+ "<br>\n"
			+ "<br>\n"
			+ "Regards,<br>\n"
			+ "DIGI ASSIST \n"
			+ "<br>\n"
			+ "<br>\n"
			+ "This message was sent from a notification-only email address that does not accept incoming email. Please do not reply to this message.\n"
			+ "<br>\n"
			+ "</body>\n"
			+ "</html>";
	
	private final String subjectPart = "Your Ticket has been {0} - Digi Assist Ticket Id {1}";
	
	private final MailjetClient client;
	
	public FirestoreMailer() {
        ClientOptions options = ClientOptions.builder()
                .apiKey(System.getenv("mailgun-apikey"))
                .apiSecretKey(System.getenv("mailgun-apisecret"))
                .build();

        client = new MailjetClient(options);
	}

	public void accept(String json, Context context) throws Exception {
		JsonObject body = gson.fromJson(json, JsonObject.class);
		logger.info("Function triggered by event on: " + context.resource());
		logger.info("Event type: " + context.eventType());
		logger.info("Attributes:");
		for (Entry<String, String> entry : context.attributes().entrySet()) {
			logger.info(entry.getKey() + "/" + entry.getValue());
		}
		logger.info("EventId: "+context.eventId());
		
		if (body != null) {
			logger.info("Body:");
			logger.info(body.toString());
		}

		boolean newTicket = body != null && body.has("oldValue") 
				&& "{}".equals(body.get("oldValue").toString());	

		String ticketId = StringUtils.substringAfterLast(context.resource(), "/");

		JsonObject newFields = body.getAsJsonObject("value").getAsJsonObject("fields");
		String query = getField(newFields, "query");
		String emailId = getField(newFields, "emailId");
		String candidateName = getField(newFields, "candidateName");

		logger.info("TicketId: "+ticketId);
		logger.info("IsNewTicket: "+newTicket);
		logger.info("Query: "+query);
		
		if (StringUtils.isBlank(ticketId)) {
			logger.info("Error! TicketId is blank. Nothing to do.");
			return;
		}		
		if (StringUtils.isBlank(query)) {
			logger.info("Error! Query is blank. Nothing to do.");
			return;
		}		
		if (StringUtils.isBlank(emailId)) {
			logger.info("Error! EmailId is blank. Nothing to do.");
			return;
		}		
		if (StringUtils.isBlank(candidateName)) {
			logger.info("Error! CandidateName is blank. Nothing to do.");
			return;
		}
		
		String subject;
		String emailBody;
		
		if (newTicket) {
			subject = MessageFormat.format(subjectPart, "Created", ticketId);
			emailBody = StringUtils.join(
					MessageFormat.format(emailBodyPart1, candidateName, "created", ticketId, query), 
					emailBodyPart3);
		} else {
			String status = getField(newFields, "status");
			logger.info("Status: "+status);
			if ("RESOLVED".equals(status)) {
				String response = getField(newFields, "response");
				logger.info("Response: "+response);
				if (StringUtils.isBlank(response)) {//this shouldn't happen
					logger.info("Error! Resolved ticket without response. Nothing to do.");
					return;
				}
				subject = MessageFormat.format(subjectPart, "Resolved", ticketId);

				emailBody = StringUtils.join(
						MessageFormat.format(emailBodyPart1, candidateName, "resolved", ticketId, query), 
						MessageFormat.format(emailBodyPart2, response),
						emailBodyPart3);
			} else {
				logger.info("Existing ticket was updated, but not resolved. Nothing to do.");
				return;
			}
		}
		
		sendEmail(emailId, candidateName, subject, emailBody);
	}
	
	private void sendEmail(String emailId, String candidateName,
			String subject, String emailBody) throws MailjetException {
		
        TransactionalEmail message = TransactionalEmail
                .builder()
                .to(new SendContact(emailId , candidateName))
                .from(new SendContact(senderEmail, senderName))
                .htmlPart(emailBody)
                .subject(subject)
                .build();

        SendEmailsRequest request = SendEmailsRequest
                .builder()
                .message(message) 
                .build();

        SendEmailsResponse response = request.sendWith(client);
        logger.info("SendEmailsResponse: "+response.toString());		
	}

	private String getField(JsonObject fields, String field) {
		if (!fields.has(field)) return null;
		JsonObject fieldJson = fields.getAsJsonObject(field);
		if (fieldJson.has("stringValue")) return fieldJson.get("stringValue").getAsString();
		
		return null;
	}

}
