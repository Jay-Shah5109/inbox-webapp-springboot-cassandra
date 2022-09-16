package io.javabrains.inbox.controllers;

import io.javabrains.inbox.email.Email;
import io.javabrains.inbox.email.EmailRepository;
import io.javabrains.inbox.email.EmailService;
import io.javabrains.inbox.emailList.EmailListItemKey;
import io.javabrains.inbox.emailList.EmailListItemRepository;
import io.javabrains.inbox.folders.Folder;
import io.javabrains.inbox.folders.FolderRepository;
import io.javabrains.inbox.folders.FolderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
public class ComposeController {

    @Autowired
    private FolderRepository folderRepository;
    @Autowired
    private FolderService folderService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private EmailRepository emailRepository;
    @Autowired
    private EmailListItemRepository emailListItemRepository;

    @GetMapping(value = "/compose")
    public String getComposePage(@RequestParam(required = false) String to,
            @RequestParam(required = false) UUID uuid,
            @AuthenticationPrincipal OAuth2User principal,
                                 Model model) {

        if (principal == null || !StringUtils.hasText(principal.getAttribute("login"))) {
            return "index";
        }
        String user = principal.getAttribute("login");

        List<Folder> userFolders = folderRepository.findAllById(user);
        model.addAttribute("userFolders", userFolders);

        List<Folder> defaultFolders = folderService.getDefaultFolders(user);
        model.addAttribute("defaultFolders", defaultFolders);
        List<String> uniqueToIds = emailService.splitIDs(to);
        model.addAttribute("toIDs", String.join(",", uniqueToIds));
        // Handling count functionality
        model.addAttribute("stats", folderService.mapCountToLabels(user));

        if (uuid == null) {
            model.addAttribute("subject", "");
            model.addAttribute("body", "");
            model.addAttribute("username", principal.getAttribute("name"));
            return "composepage";
        }

            Optional<Email> optionalEmail = emailRepository.findById(uuid);
            if (optionalEmail.isPresent()) {
                Email email = optionalEmail.get();
                if (emailService.doesHaveAccess(email, user)) {
                    model.addAttribute("subject", emailService.getReplySubject(email.getSubject()));
                    model.addAttribute("body", emailService.getReplyBody(email));
                }
            }
            Email email = optionalEmail.get();
            model.addAttribute("email", email);
        model.addAttribute("username", principal.getAttribute("name"));

        return "composepage";
    }

    @PostMapping("/sendEmail")
    public ModelAndView sendMessage(@AuthenticationPrincipal OAuth2User principal,
                                    @RequestBody MultiValueMap<String, String> formData) {

        if (principal == null || !StringUtils.hasText(principal.getAttribute("login"))) {
            return new ModelAndView("redirect:/");
        }

        String from = principal.getAttribute("login");
        List<String> toIds = emailService.splitIDs(formData.getFirst("toIds"));
        String subject = formData.getFirst("subject");
        String body = formData.getFirst("body");
        emailService.sendEmail(from, toIds, subject, body);
        return new ModelAndView("redirect:/"); // At this point, the user is authenticated, so it will
                                                        // redirect to inboxpage
    }

    @GetMapping(value = "/delete")
    public String delete(@RequestParam UUID uuid, @RequestParam String userID,
                         @RequestParam String presentFolder, @AuthenticationPrincipal OAuth2User principal, Model model) {

        if (principal == null || !StringUtils.hasText(principal.getAttribute("login"))) {
            return "inboxpage";
        }
        String user = principal.getAttribute("login");
        Optional<Email> optionalEmail = emailRepository.findById(uuid);
        if (optionalEmail.isPresent()) {
            Email email = optionalEmail.get();
            if (emailService.doesHaveAccess(email, user)) {
                emailRepository.deleteById(uuid);
                EmailListItemKey emailListItemKey = new EmailListItemKey();
                emailListItemKey.setId(user);
                emailListItemKey.setLabel(presentFolder);
                emailListItemKey.setTimeUUID(uuid);
                emailListItemRepository.deleteById(emailListItemKey);
                // Check if the email is present in Sent folder, means if the user has sent email to himself, then
                // delete from 'Sent' folder as well
                if (userID.equals(user)) {
                    emailListItemKey.setLabel("Sent");
                    emailListItemRepository.deleteById(emailListItemKey);
                }
            }
        }
        String redirectURL = "redirect:/?folder="+presentFolder;
        return redirectURL;
    }
}