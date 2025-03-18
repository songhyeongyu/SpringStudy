# servlet - exception

서블릿이 제공하는 예외

* Exception(예외)
* response.sendError(Http 상태)

## Exception(예외)

**자바 직접 실행**
자바의 메인 메서드를 직접 실행하는 경우 main 쓰레드가 실행

실행 도중 예외를 잡지 못하고 main 메서드를 넘어 예외가 던져지면 예외 정보를 남기고 쓰레드 종료

**웹**

사용자 요청별로 별도의 쓰레드가 할당 -> 서블릿 컨테이너 안에서 실행

애플리케이션에서 예외가 발생 -> try~catch로 잡아서 처리 문제 x

만약 애플리케이션에서 예외를 못 잡고, 서블릿 밖으로 까지 예외가 전달

~~~
WAS(여기까지) <- 필터 <- 서블릿 <- 인터셉터 <- 컨트롤러

결국 톰캣 같은 WAS까지 전달.
~~~

~~~java

@Controller
 public class ServletExController {
     @GetMapping("/error-ex")
     public void errorEx() {
throw new RuntimeException("예외 발생!"); }
}
~~~

웹 브라우저에서 개발자 모드로 확앤 -> HTTP 상태 코드가 500으로 보임

Exception의 경우 서버 내부에서 처리할 수 없는 오류가 발생한 것으로 생각해 HTTP 500으로 반환


## response.sendError(HTTP 상태 코드, 오류 메시지)

오류가 발생하면 HttpServletResponse가 제공하는 sendError라는 메서드를 사용해도 된다.
당장 예외가 발생하는 것은 아니지만, 서블릿 컨테이너에게 오류가 발생했다는 점을 전달할 수 있다.

* response.sendError(HTTP 상태 코드)

* response.sendError(HTTP 상태코드, 오류 메시지)

**ServletExController - 추가**

~~~java
@Controller
 public class ServletExController {
     @GetMapping("/error-ex")
     public void errorEx() {
throw new RuntimeException("예외 발생!"); }
 }
~~~

**sendError 흐름**

~~~
WAS(sendError 호출 기록 확인) <- 필터 <- 서블릿 <- 인터셉터 <- 컨트롤러 (response.sendError())
~~~

response.sendError()를 호출하면 response 내부에는 오류가 발생했다는 상태를 저장

서블릿 컨테이너는 고객 응답 전에 response에 sendError()가 호출 됐는지 확인

**정리**
서블릿 컨테이너가 제공하는 기본 예외 처리는 이상하다.

## 오류 화면 제공

서블릿 컨테이너가 제공하는 기본 예외 처리 화면은 고객 친화적이지 않다.

서블릿은 Exception이 발생해서 서블릿 밖으로 전달되거나 또는 response.sendError()가 호출 되었을 때 각각의 상황에 맞춘 오류 처리 기능을 제공

~~~java
bServerFactoryCustomizer<ConfigurableWebServerFactory> {
     @Override
     public void customize(ConfigurableWebServerFactory factory) {
         ErrorPage errorPage404 = new ErrorPage(HttpStatus.NOT_FOUND, "/error-
 page/404");
         ErrorPage errorPage500 = new ErrorPage(HttpStatus.INTERNAL_SERVER_ERROR,
 "/error-page/500");
ErrorPage errorPageEx = new ErrorPage(RuntimeException.class, "/error-page/500");
         factory.addErrorPages(errorPage404, errorPage500, errorPageEx);
} }
~~~

* response.sendError(404) : errorPage404 호출
* respoonse.sendError(500) : errorPage500 호출
* RuntimeException 또는 그 자식 타입의 예외 : errorPageEx 호출

500 예외가 서부 내부에서 발생한 오류라는 뜻을 포함하고 있기 때문에 예외가 발생한 경우도 500오류 화면 처리

오류 페이지는 예외를 다룰 때 해당 예외와 그 자식의 타입의 오류 함께 처리

runtimeException은 물론이고 그 자식까지 처ㅣㄹ

~~~java
 @Controller
 public class ErrorPageController {
     @RequestMapping("/error-page/404")
     public String errorPage404(HttpServletRequest request, HttpServletResponse
 response) {
         log.info("errorPage 404");
         return "error-page/404";
     }
       @RequestMapping("/error-page/500")
     public String errorPage500(HttpServletRequest request, HttpServletResponse
 response) {
         log.info("errorPage 500");
         return "error-page/500";
     }
}
~~~

## 오류 페이지 작동 원리

**예외 발생 흐름**
~~~
WAS(여기까지 전파) <- 필터 <- 서블릿 <- 인터셉터 <- 컨트롤러(예외발생)
~~~

**sendError 흐름**

~~~
WAS(sendError 호출 기록 확인) <- 필터 <- 서블릿 <- 인터셉터 <- 컨트롤러
 (response.sendError())
~~~

WAS는 예외를 처리하는 오류 페이지 정보 확인
`new ErrorPage(RuntimeException.class, "/error-page/500")`\

runtimeException 예외가 WAS까지 전달 -> WAS는 오류 페이지 정보 확인 -> /error-page/500이 지정 -> /error-page/500 다시 요청

**오류 페이지 요청 흐름**
~~~
WAS `/error-page/500` 다시 요청 -> 필터 -> 서블릿 -> 인터셉터 -> 컨트롤러(/error-page/500) -> View
~~~

**예외 발생과 오류 페이지 요청 흐름**

~~~
1. WAS(여기까지 전파) <- 필터 <- 서블릿 <- 인터셉터 <- 컨트롤러(예외발생)
2. WAS `/error-page/500` 다시 요청 -> 필터 -> 서블릿 -> 인터셉터 -> 컨트롤러(/error-page/
500) -> View
~~~

**중요한 점은 웹 브라우저(클라이언트)는 서버 내부에서 이런 일이 일어나는지 전혀 모른다는 점이다. 오직 서버 내부에서 오류 페이지를 찾기 위해 추가적인 호출을 한다.**

**정리**

1. 예외가 발생해서 WAS까지 전파
2. WAS는 오류 페이지 경로를 찾아 내부에서 오류 페이지를 호출 -> 필터 서블릿 인터셉터 컨트롤러가 모두 호출

## 예외 - 필터

**예외 발생과 오류 페이지 요청 흐름**
~~~
1. WAS(여기까지 전파) <- 필터 <- 서블릿 <- 인터셉터 <- 컨트롤러(예외발생)
2. WAS `/error-page/500` 다시 요청 -> 필터 -> 서블릿 -> 인터셉터 -> 컨트롤러(/error-page/
500) -> View
~~~

이러한 방식은 매우 비효율적이다 오류 페이지를 호출한다고 해서 해당 필터나 인터셉트가 한번 더 호출되기 때문이다.

따라서 DispathceType을 제공해준다.
1. 고객이 요청 하면 REQUEST가 된다.
2. 에러면 ERROR가 반환된다.
~~~java
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
             log.info("REQUEST  [{}][{}][{}]", uuid, request.getDispatcherType(),
 requestURI);
             chain.doFilter(request, response);
         } catch (Exception e) {
             throw e;
         } finally {
             log.info("RESPONSE [{}][{}][{}]", uuid, request.getDispatcherType(),
 requestURI);
         }
}
@Override
     public void destroy() {
         log.info("log filter destroy");
     }
~~~

**WebConfig**

~~~java
 @Configuration
 public class WebConfig implements WebMvcConfigurer {
     @Bean
     public FilterRegistrationBean logFilter() {
         FilterRegistrationBean<Filter> filterRegistrationBean = new
 FilterRegistrationBean<>();
         filterRegistrationBean.setFilter(new LogFilter());
         filterRegistrationBean.setOrder(1);
         filterRegistrationBean.addUrlPatterns("/*");
         filterRegistrationBean.setDispatcherTypes(DispatcherType.REQUEST,
 DispatcherType.ERROR);
         return filterRegistrationBean;
}
~~~

## 예외 - 인터셉터

~~~java

 @Slf4j
 public class LogInterceptor implements HandlerInterceptor {
     public static final String LOG_ID = "logId";
@Override
     public boolean preHandle(HttpServletRequest request, HttpServletResponse
 response, Object handler) throws Exception {
         String requestURI = request.getRequestURI();
         String uuid = UUID.randomUUID().toString();
         request.setAttribute(LOG_ID, uuid);
         log.info("REQUEST  [{}][{}][{}][{}]", uuid, request.getDispatcherType(),
 requestURI, handler);
         return true;
     }
@Override
public void postHandle(HttpServletRequest request, HttpServletResponse
 response, Object handler, ModelAndView modelAndView) throws Exception {
         log.info("postHandle [{}]", modelAndView);
     }
@Override
     public void afterCompletion(HttpServletRequest request, HttpServletResponse
 response, Object handler, Exception ex) throws Exception {
         String requestURI = request.getRequestURI();
         String logId = (String)request.getAttribute(LOG_ID);
         log.info("RESPONSE [{}][{}][{}]", logId, request.getDispatcherType(),
 requestURI);
         if (ex != null) {
             log.error("afterCompletion error!!", ex);
         }
}
~~~

DispatcherType인 경우 필터를 적용할 지 선택할 수 있었음

서블릿이 제공하는 기능이 아니라 스프링이 제공하는 기능이기 때문에 DispatcherType과 무관하게 항상 호출된다.

대신 인터셉터는 **요청 경로에 따라 추가나 제외하기 쉽게 되어있다**

~~~java
@Override
public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LogInterceptor())
        .order(1)
        .addPathPatterns("/**")
        .excludePathPatterns("/css/**", "/*.ico"
, "/error", "/error-page/**"
); }
 //오류 페이지 경로
~~~

**전체 흐름 정리**


1. 정상 요청

~~~
WAS(/hello, dispatchType=REQUEST) -> 필터 -> 서블릿 -> 인터셉터 -> 컨트롤러 -> View
~~~

2. /error-ex 요청
* 필터는 DispatchType으로 중복 호출 제거
* 인터셉터는 경로 정보로 호출 중복 호출 제거

~~~
1. WAS(/error-ex, dispatchType=REQUEST) -> 필터 -> 서블릿 -> 인터셉터 -> 컨트롤러

2. WAS(여기까지 전파) <- 필터 <- 서블릿 <- 인터셉터 <- 컨트롤러(예외발생)
3. WAS 오류 페이지 확인
4. WAS(/error-page/500, dispatchType=ERROR) -> 필터(x) -> 서블릿 -> 인터셉터(x) -> 컨트롤러(/error-page/500) -> View
~~~


## 스프링 부트 - 오류 1

지금까지 예외 처리 페이지를 만들기 위해 복잡한 과정을 거침

**스프링 부트는 이런 과정을 모두 기본으로 제공한다**

* ErrorPage를 자동으로 등록 -> /error라는 경로로 기본 오류 페이지 설정
    * new ErrorPage("/error"), 샅애 코드와 예외를 설정하지 않으면 기본 오류 페이지
    * 서블릿 밖으로 예외 발생 or response.sendError가 호출되면 /error호출

* BasicErrorController라는 스프링 컨트롤러를 자동으로 등록

**개발자는 오류 페이지만 등록**

`BasicErrorController` 는 기본적인 로직이 모두 개발되어 있다.

뷰 선택 우선순위** `BasicErrorController` 의 처리 순서

1. 뷰템플릿
* `resources/templates/error/500.html` 
* `resources/templates/error/5xx.html`
2. 정적 리소스( `static` , `public` ) 
* `resources/static/error/400.html`
* `resources/static/error/404.html`
* `resources/static/error/4xx.html`
3. 적용 대상이 없을 때 뷰 이름(`error`) 
* `resources/templates/error.html`

해당 경로 위치에 HTTP 상태 코드 이름의 뷰 파일을 넣어두면 된다.

## 스프링 부트 - 오류 페이지 2

`application.properties`
* `server.error.include-exception=false` : `exception` 포함 여부( `true` , `false` ) 
* `server.error.include-message=never` : `message` 포함 여부
* `server.error.include-stacktrace=never` : `trace` 포함 여부 
* `server.error.include-binding-errors=never` : `errors ` 포함 여부

기본 값이 never인 부분은 
* never 사용 x
* always 항상 사용
* on_param 파라미터가 있을 때

`on_param` 은 파라미터가 있으면 해당 정보를 노출한다. 디버그 시 문제를 확인하기 위해 사용할 수 있다. 그런데 이 부분도 개발 서버에서 사용할 수 있지만, 운영 서버에서는 권장하지 않는다.

**확장 포인트**
에러 공통 처리 컨트롤러의 기능을 변경하고 싶으면 `ErrorController` 인터페이스를 상속 받아서 구현하거나
`BasicErrorController` 상속 받아서 기능을 추가하면 된다.


**정리**
스프링 부트가 기본으로 제공하는 오류 페이지를 활용하면 오류 페이지와 관련된 대부분의 문제는 손쉽게 해결할 수 있 다.
