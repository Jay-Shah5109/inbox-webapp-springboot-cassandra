package io.javabrains.inbox.folders;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FolderService {

    @Autowired
    private UnreadEmailStatsRepository unreadEmailStatsRepository;

    public List<Folder> getDefaultFolders(String userID) {
        return Arrays.asList(
                new Folder(userID, "Inbox", "Blue"),
                new Folder(userID, "Sent", "Green"),
                new Folder(userID, "Important", "Red")
        );
    }

    public Map<String, Integer> mapCountToLabels(String user) {
        List<UnreadEmailStats> unreadEmailStats = unreadEmailStatsRepository.findAllById(user);
        return unreadEmailStats.stream().collect(Collectors.toMap(UnreadEmailStats::getLabel,
                UnreadEmailStats::getUnreadCount));
    }
}
