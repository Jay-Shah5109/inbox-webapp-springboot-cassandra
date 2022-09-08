package io.javabrains.inbox.folders;

import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class FolderService {

    public List<Folder> getDefaultFolders(String userID) {
        return Arrays.asList(
                new Folder(userID, "Inbox", "Blue"),
                new Folder(userID, "Sent", "Green"),
                new Folder(userID, "Important", "Red")
        );
    }
}
