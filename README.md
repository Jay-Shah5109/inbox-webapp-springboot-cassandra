# Inbox-Application
A web-application has been created to send and recieve emails (similar to Gmail, Outlook etc) on the basis of Github IDs as personal indentifier

# Technology Stack

* **Application Tier:** Java8, Spring Boot
* **Database:** Apache Cassandra using DataStax Astra DBaaS
* **Frontend:** Thymeleaf
* **Security:** Spring Security

# Functional Requirements

* **Send an email** - The user can send email to himself, or to other users via their Github IDs
* **View all emails** - The user can view all emails under specific folder as selected (Default folder is inbox)
* **Compose an email** - The user can send emails to multiple ids at once, and it should populate the inbox for all recipients mentioned in 'To' , and that email will appear in the sent items for the user
* **Reply, ReplyAll feature** - The user can use the 'Reply' or 'Reply All' functionality to reply over an email
* **View a single message** - The user can view emails as required
* **Counter for messages** - The counter will increase when email will be received, and decrease when email is read
* **Creation of new folders** - The user can create new folders as required using 'Add New Folder' option
* **Folder/label** - Default folders : Inbox, Sent, Important (Initially, the user will receive email in inbox folder, and from there the user will have the option to move the email from inbox to other folders as required)
* **Send emails from one folder to another** - The user can move emails from one folder to another using 'Move To Folder'
* **Delete email functionality** - The email gets deleted from the folder the email was present, and also from the 'Sent' folder if the user deletes email that was sent to himself

# Non-Functional Requirements

* Highly available
* High Scalable
* Security - Authentication and Authorization - leveraged Spring Security, and used login with Github
