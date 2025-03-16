package hello.login.web.session;

import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SessionManager {

    public static final String MY_SESSION_ID = "mySessionId";
    private Map<String, Object> sessionStore = new ConcurrentHashMap<>();

    /**
     * 세션 설정
     */

    public void createSession(Object value, HttpServletResponse response) {
        //세션 id를 생성하고, 값을 세션에 저장
        String sessionId = UUID.randomUUID().toString();
        sessionStore.put(sessionId, value);

        //쿠키 생성

        Cookie mySessionCookie = new Cookie(MY_SESSION_ID, sessionId);
        response.addCookie(mySessionCookie);


    }

    /**
     * 조회
     */

    public Object getSession(HttpServletRequest request) {
        Cookie sessionCookie = findCookie(request, MY_SESSION_ID);
        if (sessionCookie == null) {
            return null;
        }

        return sessionStore.get(sessionCookie.getValue());
    }

    public Cookie findCookie(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        return Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(cookieName))
                .findAny()
                .orElse(null);
    }

    /**
     * 만료
     */

    public void expire(HttpServletRequest request) {
        Cookie sessionCookie = findCookie(request, MY_SESSION_ID);
        if (sessionCookie != null) {
            sessionStore.remove(sessionCookie.getValue());
        }

    }
}
