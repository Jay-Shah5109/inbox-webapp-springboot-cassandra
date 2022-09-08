package io.javabrains.inbox;

import com.datastax.oss.driver.api.core.uuid.Uuids;
import io.javabrains.inbox.email.Email;
import io.javabrains.inbox.email.EmailRepository;
import io.javabrains.inbox.emailList.EmailListItem;
import io.javabrains.inbox.emailList.EmailListItemKey;
import io.javabrains.inbox.emailList.EmailListItemRepository;
import io.javabrains.inbox.folders.Folder;
import io.javabrains.inbox.folders.FolderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.cassandra.CqlSessionBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.nio.file.Path;
import java.util.Arrays;

@SpringBootApplication
@RestController
@EnableConfigurationProperties(DataStaxAstraProperties.class)
public class InboxApplication {

	@Autowired
	FolderRepository folderRepository;
	@Autowired
	EmailListItemRepository emailListItemRepository;
	@Autowired
	EmailRepository emailRepository;

	public static void main(String[] args) {
		SpringApplication.run(InboxApplication.class, args);
	}

	@Bean
	public CqlSessionBuilderCustomizer sessionBuilderCustomizer(DataStaxAstraProperties
																		dataStaxAstraProperties) {
		System.out.println("datastax astra prop:"+dataStaxAstraProperties.getSecureConnectBundle().toPath());
		Path bundle = dataStaxAstraProperties.getSecureConnectBundle().toPath();
		System.out.println("Bundle:"+bundle);
		return cqlSessionBuilder -> cqlSessionBuilder.withCloudSecureConnectBundle(bundle);
	}

	@PostConstruct
	public void init() {
		Folder folder = new Folder("Jay-Shah5109", "Inbox", "blue");
		folderRepository.save(folder);
		folderRepository.save(new Folder("Jay-Shah5109", "Sent", "red"));

		for (int i = 0; i < 10; i++) {
			EmailListItemKey emailListItemKey = new EmailListItemKey();
			emailListItemKey.setId("Jay-Shah5109");
			emailListItemKey.setLabel("Inbox");
			emailListItemKey.setTimeUUID(Uuids.timeBased());

			EmailListItem emailListItem = new EmailListItem();
			emailListItem.setKey(emailListItemKey);
			emailListItem.setTo(Arrays.asList("Jay-Shah5109"));
			emailListItem.setSubject("Subject: "+i);
			emailListItem.setUnread(true);

			emailListItemRepository.save(emailListItem);

			Email email = new Email();
			email.setId(emailListItemKey.getTimeUUID());
			email.setTo(emailListItem.getTo());
			email.setSubject(emailListItem.getSubject());
			email.setFrom("Jay-Shah5109");
			email.setTo(emailListItem.getTo());
			email.setBody("Body"+i);

			emailRepository.save(email);

		}
	}

}
