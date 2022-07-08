package com.digiassist.mailer.domain;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Email {

	String fromEmail;
	String fromName;
	String toEmail;
	String toName;
	String subject;
	String body;
}
