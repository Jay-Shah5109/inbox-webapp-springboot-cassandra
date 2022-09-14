package io.javabrains.inbox.email;

import com.datastax.oss.driver.api.core.uuid.Uuids;
import io.javabrains.inbox.emailList.EmailListItem;
import io.javabrains.inbox.emailList.EmailListItemKey;
import io.javabrains.inbox.emailList.EmailListItemRepository;
import io.javabrains.inbox.folders.UnreadEmailStatsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmailService {

    @Autowired
    private EmailRepository emailRepository;
    @Autowired
    private EmailListItemRepository emailListItemRepository;
    @Autowired
    private UnreadEmailStatsRepository unreadEmailStatsRepository;

    public void sendEmail(String from, List<String> to, String subject, String body) {

        Email email = new Email();
        email.setTo(to);
        email.setBody(body);
        email.setSubject(subject);
        email.setFrom(from);
        email.setId(Uuids.timeBased());
        emailRepository.save(email);

        to.forEach(id -> {
            EmailListItem emailListItem = createEmailListItem(to, subject, email, id, "Inbox");
            emailListItemRepository.save(emailListItem);
            unreadEmailStatsRepository.incrementCount(id, "Inbox");
        });

        EmailListItem sentItems = createEmailListItem(to, subject, email, from, "Sent");
        sentItems.setUnread(false);
        emailListItemRepository.save(sentItems);

    }

    private EmailListItem createEmailListItem(List<String> to, String subject, Email email, String itemOwner,
                                              String folder) {
        EmailListItemKey emailListItemKey = new EmailListItemKey();
        emailListItemKey.setId(itemOwner);
        emailListItemKey.setLabel(folder);
        emailListItemKey.setTimeUUID(email.getId());
        EmailListItem emailListItem = new EmailListItem();
        emailListItem.setKey(emailListItemKey);
        emailListItem.setTo(to);
        emailListItem.setSubject(subject);
        emailListItem.setUnread(true);
        return emailListItem;
    }

    public boolean doesHaveAccess(Email email, String user) {
        if (!email.getFrom().equals(user) && !email.getTo().contains(user)) {
            return false;
        }
        return true;
    }

    public String getReplySubject(String subject) {
        if (subject.substring(0,3).equals("Re:")) {
            return subject;
        }
        return "Re: "+subject;
    }

    public String getReplyBody(Email email) {
        return "\n\n------------------------------------------------------------- \n\n"+
                "From: "+email.getFrom()+"\n"+
                "To: "+email.getTo()+"\n\n"+
                email.getBody();
    }

    public List<String> splitIDs(String to) {
        if (!StringUtils.hasText(to)) {
            return new ArrayList<String>();
        }
        String[] splitIDs = to.split(",");
        List<String> uniqueToIds = Arrays.asList(splitIDs).stream()
                .map(id -> StringUtils.trimWhitespace(id))
                .filter(id -> StringUtils.hasText(id))
                .distinct()
                .collect(Collectors.toList());
        return uniqueToIds;
    }
}
