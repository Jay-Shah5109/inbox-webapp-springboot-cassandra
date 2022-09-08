package io.javabrains.inbox.controllers;

import io.javabrains.inbox.email.Email;
import io.javabrains.inbox.email.EmailRepository;
import io.javabrains.inbox.folders.Folder;
import io.javabrains.inbox.folders.FolderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
public class EmailController {

    @Autowired
    private FolderRepository folderRepository;
    @Autowired
    private EmailRepository emailRepository;

    @GetMapping(value = "/emails/{id}")
    public String emailView(@PathVariable UUID id, @AuthenticationPrincipal OAuth2User principal,
                            Model model) {

        if (principal == null || !StringUtils.hasText(principal.getAttribute("login"))) {
            return "index";
        }
        String user = principal.getAttribute("login");
        List<Folder> userFolders = folderRepository.findAllById(user);
        List<Folder> folders = folderRepository.findAll();
        model.addAttribute("userFolders", userFolders);
        model.addAttribute("folders", folders);
        model.addAttribute("username", user);

        Optional<Email> optionalEmail = emailRepository.findById(id);
        if (!optionalEmail.isPresent()) {
            return "inboxpage";
        }
        Email email = optionalEmail.get();
        String toIds = String.join(", ",email.getTo()); // comma seperated values for list of strings in 'To'
        model.addAttribute("email", email);
        model.addAttribute("toIds",toIds);
        return "emailpage";
    }
}