package io.javabrains.inbox.controllers;

import io.javabrains.inbox.folders.Folder;
import io.javabrains.inbox.folders.FolderRepository;
import io.javabrains.inbox.folders.FolderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ComposeController {

    @Autowired
    private FolderRepository folderRepository;
    @Autowired
    private FolderService folderService;

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

        if (StringUtils.hasText(to)) {
            String[] splitIDs = to.split(",");
            List<String> uniqueToIds = Arrays.asList(splitIDs).stream()
                    .map(id -> StringUtils.trimWhitespace(id))
                    .filter(id -> StringUtils.hasText(id))
                    .distinct()
                    .collect(Collectors.toList());

            model.addAttribute("toIDs", String.join(",", uniqueToIds));
        }

        model.addAttribute("username", user);

        return "composepage";
    }
}
