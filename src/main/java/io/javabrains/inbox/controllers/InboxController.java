package io.javabrains.inbox.controllers;

import com.datastax.oss.driver.api.core.uuid.Uuids;
import io.javabrains.inbox.emailList.EmailListItem;
import io.javabrains.inbox.emailList.EmailListItemRepository;
import io.javabrains.inbox.folders.Folder;
import io.javabrains.inbox.folders.FolderRepository;
import io.javabrains.inbox.folders.FolderService;
import org.ocpsoft.prettytime.PrettyTime;
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

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Controller
public class InboxController {

    @Autowired
    private FolderRepository folderRepository;
    @Autowired
    private EmailListItemRepository emailListItemRepository;
    @Autowired
    private FolderService folderService;

    @GetMapping(value = "/")
    public String homePage(@RequestParam(required = false) String folder, @AuthenticationPrincipal OAuth2User principal,
                           Model model) {
        if (principal == null || !StringUtils.hasText(principal.getAttribute("login"))) {
            return "index";
        }
        String user = principal.getAttribute("login");

        // Fetch folders

        List<Folder> userFolders = folderRepository.findAllById(user);

        List<Folder> folders = folderRepository.findAll();
        model.addAttribute("userFolders", userFolders);

        List<Folder> defaultFolders = folderService.getDefaultFolders(user);
        model.addAttribute("defaultFolders", defaultFolders);

        // Handling count functionality
        model.addAttribute("stats", folderService.mapCountToLabels(user));

//        model.addAttribute("folders", folders);
        model.addAttribute("username", principal.getAttribute("name"));

        // Fetch messages

        if (!StringUtils.hasText(folder)) {
            folder = "Inbox";
        }

//        String folderLabel = "Inbox";
        List<EmailListItem> emailList = emailListItemRepository.findAllByKey_IdAndKey_Label(user, folder);

        PrettyTime prettyTime = new PrettyTime();
        emailList.stream().forEach(emailListItem -> {
            UUID uuid = emailListItem.getKey().getTimeUUID();
            Date emailDateTime = new Date(Uuids.unixTimestamp(uuid));
            emailListItem.setAgoTimeString(prettyTime.format(emailDateTime));
        });

        model.addAttribute("emailList", emailList);
        model.addAttribute("folderName", folder);

        return "inboxpage";
    }

    @PostMapping("/")
    public ModelAndView sendMessage(@AuthenticationPrincipal OAuth2User principal,
                                    @RequestBody(required = false) MultiValueMap<String, String> formData) {

        if (principal == null || !StringUtils.hasText(principal.getAttribute("login"))) {
            return new ModelAndView("redirect:/");
        }
        String user = principal.getAttribute("login");
        String foldername = formData.getFirst("foldername");
        Folder newFolder = new Folder(user, foldername, "blue");
        folderRepository.save(newFolder);
        return new ModelAndView("redirect:/"); // At this point, the user is authenticated, so it will
        // redirect to inboxpage
    }
}
