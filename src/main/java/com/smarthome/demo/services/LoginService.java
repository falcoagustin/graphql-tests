package com.smarthome.demo.services;

import com.smarthome.demo.domain.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.collections4.map.PassiveExpiringMap;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@Component
@Getter
@AllArgsConstructor
// Really dummy in-memory login service.
public class LoginService {
    private final long EXPIRE_TIMEOUT = 24 * 60 * 1000; // Twenty-four hours
    private static final ThreadLocal<User> currentUserLocal = new ThreadLocal<>();
    private final Map<Long, String> cookies = Collections.synchronizedMap(new PassiveExpiringMap<>(EXPIRE_TIMEOUT));

    public String login(User user) {
        String cookie = UUID.randomUUID().toString();
        cookies.put(user.getId(), cookie);
        currentUserLocal.set(user);
        return cookie;
    }

    public boolean isLoggedIn(User user) {
        return cookies.containsKey(user.getId());
    }

    public void logout(User user) {
        cookies.remove(user.getId());
        currentUserLocal.remove();
    }
}
