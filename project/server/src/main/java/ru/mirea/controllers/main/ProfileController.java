package ru.mirea.controllers.main;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.mirea.controllers.AuthController;
import ru.mirea.data.SSE.TypesConnect;
import ru.mirea.data.json.Role;
import ru.mirea.data.models.auth.User;
import ru.mirea.data.models.school.Group;
import ru.mirea.data.models.school.School;
import ru.mirea.services.ServerService;

import java.util.ArrayList;
import java.util.List;

@RequestMapping("/profiles")
@NoArgsConstructor
@RestController public class ProfileController {

    @Autowired
    private Gson gson;

    @Autowired
    private ServerService datas;

    @Autowired
    private AuthController authController;

    @PostMapping
    public JsonObject post(@RequestBody JsonObject data) {
        System.out.println("Post! " + data);
        JsonObject ans = new JsonObject(), body = null, bodyAns;
        ans.addProperty("error", false);
        if(data.has("body") && data.get("body").isJsonObject()) body = data.get("body").getAsJsonObject();
        if(!data.has("type")) data.addProperty("type", "default");
        switch (data.get("type").getAsString()){
            case "getProfile" -> {
                bodyAns = new JsonObject();
                ans.add("body", bodyAns);
                User user = datas.userByLogin(body.get("login").getAsString());
                if(user != null) {
                    bodyAns.addProperty("login", user.getLogin());
                    bodyAns.addProperty("ico", user.getIco());
                    bodyAns.addProperty("id", user.getId());
                    if(!ObjectUtils.isEmpty(user.getFio())) bodyAns.addProperty("fio", user.getFio());
                    if(!ObjectUtils.isEmpty(user.getInfo())) bodyAns.addProperty("more", user.getInfo());
                    JsonObject roles = new JsonObject();
                    for(long i = 0; i < 5; i++){
                        if(!user.getRoles().containsKey(i)) continue;
                        JsonObject roleO = new JsonObject();
                        Role role = user.getRoles().get(i);
                        if(!ObjectUtils.isEmpty(role.getEmail())) roleO.addProperty("email", role.getEmail());
                        if(!ObjectUtils.isEmpty(role.getYO())) {
                            School school = datas.schoolById(role.getYO());
                            if(school != null) roleO.addProperty("yo", school.getName());
                        }
                        if(!ObjectUtils.isEmpty(role.getGroup())) {
                            Group group = datas.groupById(role.getGroup());
                            if(group != null) roleO.addProperty("group", role.getGroup());
                        }
                        if(!ObjectUtils.isEmpty(role.getSubjects())) {
                            List<String> subjects = new ArrayList<>();
                            for(Long i1 : role.getSubjects()) {
                                subjects.add(datas.subjectById(i1).getName());
                            }
                            roleO.add("lessons", gson.toJsonTree(subjects));
                        }
                        if(!ObjectUtils.isEmpty(role.getKids())){
                            JsonObject kids = new JsonObject();
                            for(Long i1 : role.getKids()){
                                JsonObject kid = new JsonObject();
                                User kidU = datas.userById(i1);
                                kid.addProperty("name", kidU.getFio());
                                kid.addProperty("login", kidU.getLogin());
                                kids.add(i1+"", kid);
                            }
                            roleO.add("kids", kids);
                        }
                        if(!ObjectUtils.isEmpty(role.getParents())){
                            JsonObject parents = new JsonObject();
                            for(Long i1 : role.getParents()){
                                JsonObject parent = new JsonObject();
                                User parentU = datas.userById(i1);
                                parent.addProperty("name", parentU.getFio());
                                parent.addProperty("login", parentU.getLogin());
                                parents.add(i1+"", parent);
                            }
                            roleO.add("parents", parents);
                        }
                        roles.add(i+"", roleO);
                    }
                    bodyAns.add("roles", roles);
                }
                return ans;
            }
            case "chLogin" -> {
                bodyAns = new JsonObject();
                ans.add("body", bodyAns);
                User user = datas.userByLogin(body.get("oLogin").getAsString());
                User userN = datas.userByLogin(body.get("nLogin").getAsString());
                if(user != null && userN == null){
                    user.setLogin(body.get("nLogin").getAsString());
                    datas.getUserRepository().saveAndFlush(user);

                    JsonObject ansToCl = new JsonObject();
                    ansToCl.addProperty("login", user.getLogin());
                    authController.sendMessageForAll("chLogin", ansToCl, TypesConnect.PROFILES, user.getLogin(), "main", "main", "main");
                } else {
                    ans.addProperty("error", true);
                }
                return ans;
            }
            case "chInfo" -> {
                bodyAns = new JsonObject();
                ans.add("body", bodyAns);
                User user = datas.userByLogin(body.get("login").getAsString());
                if(user != null) {
                    user.setInfo(body.get("info").getAsString());
                    datas.getUserRepository().saveAndFlush(user);

                    JsonObject ansToCl = new JsonObject();
                    ansToCl.addProperty("more", user.getInfo());
                    authController.sendMessageForAll("chInfo", ansToCl, TypesConnect.PROFILES, user.getLogin(), "main", "main", "main");
                } else {
                    ans.addProperty("error", true);
                }
                return ans;
            }
            case "chEmail" -> {
                bodyAns = new JsonObject();
                ans.add("body", bodyAns);
                User user = datas.userByLogin(body.get("login").getAsString());
                if(user != null) {
                    user.getRoles().get(body.get("role").getAsLong()).setEmail(body.get("email").getAsString());
                    datas.getUserRepository().saveAndFlush(user);

                    JsonObject ansToCl = new JsonObject();
                    ansToCl.addProperty("email", body.get("email").getAsString());
                    ansToCl.addProperty("role", body.get("role").getAsLong());
                    authController.sendMessageForAll("chEmail", ansToCl, TypesConnect.PROFILES, user.getLogin(), "main", "main", "main");
                } else {
                    ans.addProperty("error", true);
                }
                return ans;
            }
            default -> {
                System.out.println("Error Type" + data.get("type"));
                ans.addProperty("error", true);
                return ans;
            }
        }
    }
}