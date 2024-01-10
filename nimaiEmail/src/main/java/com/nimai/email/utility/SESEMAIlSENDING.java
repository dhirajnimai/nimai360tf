package com.nimai.email.utility;
import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class SESEMAIlSENDING {


	public void sendEmail() {
		final String body = "This is an example body!";
		final String subject = "This is an example subject!";

		// Create a Properties object to contain connection configuration information.
		Properties props = System.getProperties();
		props.put("mail.transport.protocol", "smtps");
		props.put("mail.smtp.port", 587);    // this port number varies depending on your connection method

		// Set properties indicating that we want to use STARTTLS to encrypt the connection.
		// The SMTP session will begin on an unencrypted connection, and then the client
		// will issue a STARTTLS command to upgrade to an encrypted connection.
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.starttls.required", "true");
		

		// Create a Session object to represent a mail session with the specified properties.
		Session session = Session.getDefaultInstance(props);

		// Create a message with the specified information.
		MimeMessage msg = new MimeMessage(session);
		try {
      msg.setFrom(new InternetAddress("dev_notification@360tf.trade"));
      msg.setRecipient(Message.RecipientType.TO, new InternetAddress("dhiraj.jagtap@360tf.trade"));
			msg.setSubject(subject);
			msg.setContent(body, "text/plain");
		} catch (MessagingException e) {
			System.out.println("ERROR: " + e.toString());
		}

		// Create a transport.
		Transport transport = null;
		try {
			transport = session.getTransport();
		} catch (NoSuchProviderException e) {
			System.out.println("ERROR: " + e.toString());
		}

		// Send the message.
		try {
			// Connect to Amazon SES using the SMTP USERNAME and PASSWORD you specified above
      // https://docs.aws.amazon.com/ses/latest/DeveloperGuide/smtp-credentials.html
			transport.connect
			("email-smtp.us-west-1.amazonaws.com", "AKIAR4T6WO6SKJKY6DWQ", "BFh+AufyUXToalsrJn1IwW6rc9mfm6cWW+xHFUUMhFOi");

			// Send the email.
			transport.sendMessage(msg, msg.getAllRecipients());
		} catch (Exception e) {
			System.out.println("ERROR: " + e.toString());
		} finally {
			// Close and terminate the connection.
			try {
				transport.close();
			} catch (MessagingException e) {
				System.out.println("ERROR: " + e.toString());
			}
		}
	}
	
	
	public static void main(String[] args) {
		SESEMAIlSENDING se=new SESEMAIlSENDING();
	//	se.sendEmail();
		
		


		 final String username = "AKIAR4T6WO6SKJKY6DWQ";
		        final String password = "BFh+AufyUXToalsrJn1IwW6rc9mfm6cWW+xHFUUMhFOi";

		        Properties prop = new Properties();
				prop.put("mail.smtp.host", "email-smtp.us-east-1.amazonaws.com");
		        prop.put("mail.smtp.port", "587");
		        prop.put("mail.smtp.auth", "true");
		        
		        
				//props.put("mail.smtp.auth", "true");
				prop.put("mail.smtp.starttls.enable", "true");
				//props.put("mail.smtp.starttls.required", "true");
		        
     // prop.put("mail.smtp.socketFactory.port", "465");
		//        prop.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		        
		        Session session = Session.getInstance(prop,
		                new javax.mail.Authenticator() {
		                    protected PasswordAuthentication getPasswordAuthentication() {
		                        return new PasswordAuthentication(username, password);
		                    }
		                });

		        try {

		            Message message = new MimeMessage(session);
		            message.setFrom(new InternetAddress("dev_notification@360tf.trade"));
		            message.setRecipients(
		                    Message.RecipientType.TO,
		                    InternetAddress.parse("dhiraj.jagtap@360tf.trade")
		            );
		            message.setSubject("Testing Gmail SSL");
		            message.setText("Dear Mail Crawler,"
		                    + "\n\n Please do not spam my email!");

		            Transport.send(message);

		            System.out.println("Done");

		        } catch (MessagingException e) {
		            e.printStackTrace();
		        }
		    }

}

	

