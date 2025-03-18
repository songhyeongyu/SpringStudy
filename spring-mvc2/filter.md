# fillter

**servlet filter**

공통 사항

* 로그인 한 사용자만 상품 관리 페이지에 들어갈 수 있어야 된다. but 로그인을 하지 않은 사용자도 url을 직접 호출하면 상품 관리 화면에 들어갈 수 있다.

* 상품 관리 컨트롤러에서 로그인 여부를 체크하는 로직을 작성하면 되지만 모든 컨트롤러 로직에 공통으로 로그인 여부를 확인해야된다. -> 향후 확장을 할 때 굉장히 골치 아픔

* 여러 로직에 공통으로 관심이 있는 것을 cross-cutting concern이라고 한다.

* AOP로도 해결할 수 있지만 웹과 관련된 공통 관심사는 **interceptor** 또는 **filter**을 사용하는 것이 좋다.

* 웹과 관련된 공통 관심사를 처리할 때는 HTTP의 헤더나 url 정보들이 필요한데 -> 필터, 인터셉터는 HttpServletRequest 제공을해서 사용하면 된다.

**필터 흐름**

~~~
HTTP 요청 -> WAS -> 필터 -> 서블릿 -> 컨트롤러
~~~

* 필터를 적용하면 필터가 호출 된 다음 서블릿 호출
* 고객의 요청 로그를 남기는 요구사항이 있다면 필터를 사용
* 스프링을 사용하는 경우 서블릿은 디스패쳐 서블릿이다.

**필터 제한**
~~~
HTTP 요청 -> WAS -> 필터 -> 서블릿 -> 컨트롤러
HTTP 요청 -> WAS -> 필터(적절하지 않으면 호출 x)
~~~

**필터 체인**
~~~
HTTP 요청 -> WAS -> 필터 1 -> 필터 2 -> 서블릿 -> 컨트롤러
~~~

* 필터는 체인으로 구성되는데, 자유롭게 구성할 수 있다.


~~~java
 public interface Filter {
     public default void init(FilterConfig filterConfig) throws ServletException
 {}
     public void doFilter(ServletRequest request, ServletResponse response,
             FilterChain chain) throws IOException, ServletException;
     public default void destroy() {}
}
~~~

* `init` : 필터 초기화 메서드, 서블릿 컨테이너가 생성될 때 호출
* `doFilter()`: 고객의 요청이 올 때 마다 해당 메서드가 호출
* `destory()`: 필터 종료 메서드, 서블릿 컨테이너가 종료될 때 호출

## 요청 로그

모든 요청을 로그로 남기는 필터를 개발해보장

~~~java
@Slf4j
 public class LogFilter implements Filter {
     @Override
     public void init(FilterConfig filterConfig) throws ServletException {
         log.info("log filter init");
     }
@Override
     public void doFilter(ServletRequest request, ServletResponse response,
 FilterChain chain) throws IOException, ServletException {
         HttpServletRequest httpRequest = (HttpServletRequest) request;
         String requestURI = httpRequest.getRequestURI();
         String uuid = UUID.randomUUID().toString();
         try {
             log.info("REQUEST  [{}][{}]", uuid, requestURI);
             chain.doFilter(request, response);
         } catch (Exception e) {
             throw e;
         } finally {
             log.info("RESPONSE [{}][{}]", uuid, requestURI);
} }

  @Override
    public void destroy() {
        log.info("log filter destroy");
    }
}
~~~

* doFilter()
    * HTTP 요청이 오면 doFilter가 호출
    * ServletRequest는 HTTP 요청이 아닌 경우까지 고려해서 만들었다 -> HttpServletRequest 로 다운 케스팅을 해서 사용한다.

* uuid 
    * HTTP 요청을 구분하기 위해 요청당 임의의 uuid를 생성해둔다.

* log.info("REQUEST")
    * uuid와 requestURI를 출력

* chain.doFilter(request,response)
    * ❗️매우 중요. 다음 필터가 있으면 필터를 호출, 없으면 서블릿을 호출


**WebConfig - 필터설정**

~~~java
 @Configuration
 public class WebConfig {
     @Bean
     public FilterRegistrationBean logFilter() {
         FilterRegistrationBean<Filter> filterRegistrationBean = new
 FilterRegistrationBean<>();
 filterRegistrationBean.setFilter(new LogFilter());
         filterRegistrationBean.setOrder(1);
         filterRegistrationBean.addUrlPatterns("/*");
         return filterRegistrationBean;
}
 }
~~~

스프링 부트를 사용한다면 FilterRegistrationBean을 사용해서 등록하면 된다.

* setFilter : 등록할 필터를 지정
* setOrder : 필터는 체인으로 동작 -> 낮을 수록 먼저 동작
* addUrlPatterns("/*") : 필터를 적용할 URL 패턴을 지정한다.

실무에서 HTTP 요청시 같은 요청의 로그에 모두 같은 식별자를 자동으로 남기는 방법 -> logback mdc!

## 인증 체크

로그인 되지 않은 사용자는 상품 관리 뿐만 아니라 미래에 개발될 페이지에도 접근하지 못하도록 하자.

~~~java
@Slf4j
 public class LoginCheckFilter implements Filter {
     private static final String[] whitelist = {"/", "/members/add", "/login", "/
 logout","/css/*"};
@Override
     public void doFilter(ServletRequest request, ServletResponse response,
 FilterChain chain) throws IOException, ServletException {
         HttpServletRequest httpRequest = (HttpServletRequest) request;
         String requestURI = httpRequest.getRequestURI();
         HttpServletResponse httpResponse = (HttpServletResponse) response;
         try {
log.info("인증 체크 필터 시작 {}", requestURI);
if (isLoginCheckPath(requestURI)) {
log.info("인증 체크 로직 실행 {}", requestURI); HttpSession session = httpRequest.getSession(false); if (session == null ||
session.getAttribute(SessionConst.LOGIN_MEMBER) == null) { log.info("미인증 사용자 요청 {}", requestURI);
requestURI);
                }
//로그인으로 redirect 
httpResponse.sendRedirect("/login?redirectURL=" +
return; 
//여기가 중요, 미인증 사용자는 다음으로 진행하지 않고 끝!
}
            chain.doFilter(request, response);
        } catch (Exception e) {
throw e; //예외 로깅 가능 하지만, 톰캣까지 예외를 보내주어야 함 } finally {
log.info("인증 체크 필터 종료 {}", requestURI); }
}
/**
* 화이트 리스트의 경우 인증 체크X */
    private boolean isLoginCheckPath(String requestURI) {
        return !PatternMatchUtils.simpleMatch(whitelist, requestURI);
}
~~~
* whitelist 
    * 인증 필터를 적용해도 홈, 회원가입, 로그인 화면, css 같은 리소스에는 접근할 수 있어야 된다.

* isLoginCheckPath
    * 화이트 리스트를 제외한 모든 경우

* httpResoponse.sendRedirect("/login?redirectURL=" + requestURI);

    * 미인증 사용자는 로그인 화면으로 리다이렉트 but 로그인 이후에 다시 홈으로 이동하면 원하는 경로를 다시 찾아가야하는 불편함 존재
    * 이러한 기능을 위해 요청한 경로인 requestURI를 /login에 쿼리 파라미터로 함게 전달.

* return: 필터를 더 이상 진행하지 않는다
    * 따라서 필터는 물론 서블릿, 컨트롤러가 더는 호출 되지 않는다.
    * redirect를 사용했기때문에 redirect가 응답으로 적용되고 요청이 끝난다.

~~~java

 @Bean
 public FilterRegistrationBean loginCheckFilter() 
  FilterRegistrationBean<Filter> filterRegistrationBean = new
FilterRegistrationBean<>();
    filterRegistrationBean.setFilter(new LoginCheckFilter());
    filterRegistrationBean.setOrder(2);
    filterRegistrationBean.addUrlPatterns("/*");
    return filterRegistrationBean;
}
~~~
* setFilter(new LonginCheckFilter()) : 로그인 필터를 등록
* setOrder(2) : 순서를 2번으로 잡음
* addUrlPatterns("/*") : 모든 요청에 로그인 필터를 적용

**RedirectURL 처리**

로그인에 성공하면 처음 요청한 URL로 가기!

~~~java
 @PostMapping("/login")
 public String loginV4(
         @Valid @ModelAttribute LoginForm form, BindingResult bindingResult,
         @RequestParam(defaultValue = "/") String redirectURL,
         HttpServletRequest request) {
     if (bindingResult.hasErrors()) {
         return "login/loginForm";
}
     Member loginMember = loginService.login(form.getLoginId(),
 form.getPassword());
     log.info("login? {}", loginMember);
if (loginMember == null) {
bindingResult.reject("loginFail", "아이디 또는 비밀번호가 맞지 않습니다."); return "login/loginForm";
}
 @PostMapping("/login")
 public String loginV4(
         @Valid @ModelAttribute LoginForm form, BindingResult bindingResult,
         @RequestParam(defaultValue = "/") String redirectURL,
         HttpServletRequest request) {
     if (bindingResult.hasErrors()) {
         return "login/loginForm";
}
     Member loginMember = loginService.login(form.getLoginId(),
 form.getPassword());
     log.info("login? {}", loginMember);
if (loginMember == null) {
bindingResult.reject("loginFail", "아이디 또는 비밀번호가 맞지 않습니다."); return "login/loginForm";
}
~~~

* 로그인 체크 필터에서, 미인증 사용자는 요청 경로를 포함해서 /login에 redircetURL 요청 파라미터 추가

**정리**

서블릿 필터를 잘 사용한 덕분에 로그인 하지 않은 사용자는 나머지 경로에 들어갈 수 없게 되었다.
공통 관심사를 서블 릿 필터를 사용해서 해결한 덕분에 향후 로그인 관련 정책이 변경되어도 이 부분만 변경하면 된다.

하지만 실무에서 사용은 잘 되지 않음!!