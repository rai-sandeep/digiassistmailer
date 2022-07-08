package com.digiassist.mailer;

import com.digiassist.mailer.domain.Email;

public interface Mailer {

	public void sendEmail(Email email) throws Exception;
}
