package io.javabrains.inbox.controllers;

import io.javabrains.inbox.email.Email;
import io.javabrains.inbox.email.EmailRepository;
import io.javabrains.inbox.emailList.EmailListItem;
import io.javabrains.inbox.emailList.EmailListItemKey;
import io.javabrains.inbox.emailList.EmailListItemRepository;
import io.javabrains.inbox.folders.Folder;
import io.javabrains.inbox.folders.FolderRepository;
import io.javabrains.inbox.folders.FolderService;
import io.javabrains.inbox.folders.UnreadEmailStatsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
public class EmailController {

    @Autowired
    private FolderRepository folderRepository;
    @Autowired
    private EmailRepository emailRepository;
    @Autowired
    private FolderService folderService;
    @Autowired
    private EmailListItemRepository emailListItemRepository;
    @Autowired
    private UnreadEmailStatsRepository unreadEmailStatsRepository;

    @GetMapping(value = "/emails/{id}")
    public String emailView(@RequestParam String folder, @PathVariable UUID id,
                            @AuthenticationPrincipal OAuth2User principal, Model model) {

        if (principal == null || !StringUtils.hasText(principal.getAttribute("login"))) {
            return "index";
        }
        String user = principal.getAttribute("login");

        List<Folder> userFolders = folderRepository.findAllById(user);
        model.addAttribute("userFolders", userFolders);

        List<Folder> defaultFolders = folderService.getDefaultFolders(user);
        model.addAttribute("defaultFolders", defaultFolders);

        model.addAttribute("username", user);

        Optional<Email> optionalEmail = emailRepository.findById(id);
        if (!optionalEmail.isPresent()) {
            return "inboxpage";
        }
        Email email = optionalEmail.get();
        String toIds = String.join(", ",email.getTo()); // comma seperated values for list of strings in 'To'
        model.addAttribute("email", email);
        model.addAttribute("toIds",toIds);

        EmailListItemKey emailListItemKey = new EmailListItemKey();
        emailListItemKey.setLabel(folder);
        emailListItemKey.setTimeUUID(email.getId());
        emailListItemKey.setId(user);

        Optional<EmailListItem> optionalEmailListItem = emailListItemRepository.findById(emailListItemKey);
        if (optionalEmailListItem.isPresent()) {
            EmailListItem emailListItem = optionalEmailListItem.get();
            if (emailListItem.isUnread()) {
                emailListItem.setUnread(false);
                emailListItemRepository.save(emailListItem);
                unreadEmailStatsRepository.decrementCount(user, folder);
            }
        }

        // Handling count functionality
        model.addAttribute("stats", folderService.mapCountToLabels(user));

        return "emailpage";
    }
}