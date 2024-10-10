## Liferay emails plugin client extension 
 This plugin automates the process of retrieving emails from a designated mailbox and assigning them as actionable work items in workflow. This solution is ideal for teams looking to streamline workflow management, enhance productivity, and ensure timely responses to incoming communications.
 this client extension leverage  

## Configuration
Clone the repo `git clone`, navigate to `/client-extensions/m9a28-email-etc-spring-boot/src/main/resources/application-default.properties` and edit the following lines to match your own credentials.

```
com.liferay.email.host=
com.liferay.email.username=
com.liferay.email.password=
```

### For gmail
```
com.liferay.email.host=imap.gmail.com
com.liferay.email.username=manwah.ayassor@gmail.com (change to your email)
com.liferay.email.password=<your-app-password> Replace `<your-app-password>` with your app password generated from your email account
```
learn more on google app passwords  https://support.google.com/mail/answer/185833?hl=fr

### For Yahoo
com.liferay.email.host=imap.mail.yahoo.com

## Your Feedback (^_^)
 
I would appreciate any feedback to help improve this plugin.