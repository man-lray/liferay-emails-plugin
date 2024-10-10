/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.email;

import com.liferay.email.service.EmailService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import jakarta.mail.*;
import jakarta.mail.search.FlagTerm;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Properties;
import java.util.TimeZone;

/**
 * @author Raymond Augé
 * @author Gregory Amerson
 * @author Brian Wing Shun Chan
 */
@RequestMapping("/emails")
@RestController
public class EmailRestController extends BaseRestController {

    @Autowired
    private EmailService emailService;

    @PostMapping("/fetch-recent")
    public ResponseEntity<String> post(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody String json) {

        System.out.println(json);

        // Create properties for the session
        Properties properties = new Properties();
        properties.put("mail.imap.host", host);
        properties.put("mail.imap.port", "993");
        properties.put("mail.imap.starttls.enable", "true");
        properties.put("mail.imap.ssl.trust", host); // Trust the host
        properties.put("mail.imap.ssl.enable", "true"); // SSL for security

        // Get the session object
        Session session = Session.getInstance(properties, null);

        try {
            // Connect to the email store
            Store store = session.getStore("imap");
            store.connect(username, password);

            // Access the inbox folder
            Folder inbox = store.getFolder("inbox");
            inbox.open(Folder.READ_ONLY);

            // Search for unread emails
            FlagTerm unreadFlagTerm = new FlagTerm(new Flags(Flags.Flag.SEEN), false);
            Message[] unreadMessages = inbox.search(unreadFlagTerm);

            // Get the number of unread messages
            int totalUnreadMessages = unreadMessages.length;

            // Determine the range for the last 10 unread messages
            int start = Math.max(totalUnreadMessages - 10, 0);  // Start at least from 0
            int end = totalUnreadMessages;  // End with the total number of unread messages

            // Fetch the last 10 unread messages
            Message[] messages = new Message[end - start];
            if (end > start) {
                System.arraycopy(unreadMessages, start, messages, 0, end - start);
            }

            System.out.println("Fetching the last " + messages.length + " unread emails:");
            for (Message message : messages) {
                // Print email details
//                System.out.println("---------------------------------");

//                System.out.println("Subject: " + message.getSubject());
//                System.out.println("From: " + message.getFrom()[0]);
//                System.out.println("Sent Date: " + message.getSentDate());

                // Create a SimpleDateFormat for parsing the input date
                SimpleDateFormat inputFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
                inputFormat.setTimeZone(TimeZone.getTimeZone("Europe/Lisbon")); // Set the proper timezone

                // Parse the input date string to a Date object
                Date date = inputFormat.parse(message.getSentDate().toString());

                // Convert the Date object to Instant
                Instant instant = date.toInstant();

                // Convert the Instant to ZonedDateTime in UTC
                ZonedDateTime zonedDateTime = instant.atZone(ZoneId.of("UTC"));

                // Format ZonedDateTime as ISO 8601 string
                String sentDate = DateTimeFormatter.ISO_INSTANT.format(zonedDateTime);

                // Post to emails object
                ResponseEntity<String> newEmail = emailService.create(jwt,
                        String.valueOf(new JSONObject()
                                .put("subject", message.getSubject())
                                .put("from", message.getFrom()[0])
                                .put("sentDate", sentDate)
                        )
                );

                // Print newEmail object
                System.out.println(newEmail);
            }

            // Close the inbox folder and the store connection
            inbox.close(false);
            store.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity(json, HttpStatus.OK);
    }

    @Value("${com.liferay.email.host}")
    protected String host;
    @Value("${com.liferay.email.username}")
    protected String username;
    @Value("${com.liferay.email.password}")
    protected String password;


    private static final Log _log = LogFactory.getLog(
            EmailRestController.class);

}