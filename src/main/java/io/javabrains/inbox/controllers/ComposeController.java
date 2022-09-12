package io.javabrains.inbox.controllers;

import io.javabrains.inbox.email.EmailService;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ComposeController {

    @Autowired
    private FolderRepository folderRepository;
    @Autowired
    private FolderService folderService;
    @Autowired
    private EmailService emailService;

    @GetMapping(value = "/compose")
    public String getComposePage(@RequestParam(required = false) String to,
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
        List<String> uniqueToIds = splitIDs(to);
        model.addAttribute("toIDs", String.join(",", uniqueToIds));
        // Handling count functionality
        model.addAttribute("stats", folderService.mapCountToLabels(user));

        model.addAttribute("username", principal.getAttribute("name"));

        return "composepage";
    }

    private List<String> splitIDs(String to) {
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

    @PostMapping("/sendEmail")
    public ModelAndView sendMessage(@AuthenticationPrincipal OAuth2User principal,
                                    @RequestBody MultiValueMap<String, String> formData) {

        if (principal == null || !StringUtils.hasText(principal.getAttribute("login"))) {
            return new ModelAndView("redirect:/");
        }

        String from = principal.getAttribute("login");
        List<String> toIds = splitIDs(formData.getFirst("toIds"));
        String subject = formData.getFirst("subject");
        String body = formData.getFirst("body");
        emailService.sendEmail(from, toIds, subject, body);
        return new ModelAndView("redirect:/"); // At this point, the user is authenticated, so it will
                                                        // redirect to inboxpage
    }
}
