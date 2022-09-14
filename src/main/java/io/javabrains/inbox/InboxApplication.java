package io.javabrains.inbox;

import io.javabrains.inbox.email.EmailService;
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
	private FolderRepository folderRepository;
	@Autowired
	private EmailService emailService;

	public static void main(String[] args) {
		SpringApplication.run(InboxApplication.class, args);
	}

	@Bean
	public CqlSessionBuilderCustomizer sessionBuilderCustomizer(DataStaxAstraProperties
																		dataStaxAstraProperties) {
		Path bundle = dataStaxAstraProperties.getSecureConnectBundle().toPath();
		return cqlSessionBuilder -> cqlSessionBuilder.withCloudSecureConnectBundle(bundle);
	}

	@PostConstruct
	public void init() {

		// User customized folders
		Folder folder = new Folder("Jay-Shah5109", "Work", "blue");
		folderRepository.save(folder);
		folderRepository.save(new Folder("Jay-Shah5109", "Family", "red"));

		for (int i = 0; i < 10; i++) {
			emailService.sendEmail("Jay-Shah5109", Arrays.asList("Jay-Shah5109","abc"), "Subject:"+i,
					"Body:"+i);
		}

		emailService.sendEmail("abc", Arrays.asList("def","abc"), "Subject: Unknown",
				"Body: Unknown");
	}

}
