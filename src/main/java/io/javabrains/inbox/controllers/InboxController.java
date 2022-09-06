package io.javabrains.inbox.controllers;

import io.javabrains.inbox.emailList.EmailListItem;
import io.javabrains.inbox.emailList.EmailListItemRepository;
import io.javabrains.inbox.folders.Folder;
import io.javabrains.inbox.folders.FolderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class InboxController {

    @Autowired
    private FolderRepository folderRepository;
    @Autowired
    private EmailListItemRepository emailListItemRepository;

    @GetMapping(value = "/")
    public String homePage(@AuthenticationPrincipal OAuth2User principal, Model model) {
        if (principal == null || !StringUtils.hasText(principal.getAttribute("login"))) {
            return "index";
        }
        String user = principal.getAttribute("login");
        List<Folder> userFolders = folderRepository.findAllById(user);
        model.addAttribute("userFolders", userFolders);

        String folderLabel = "Inbox";
        List<EmailListItem> emailList = emailListItemRepository.findAllByKey_IdAndKey_Label(user, folderLabel);
        model.addAttribute("emailList", emailList);

        return "inboxpage";
    }
}
