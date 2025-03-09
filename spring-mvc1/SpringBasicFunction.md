# logging

**로깅 라이브러리**

인터페이스로 SLF4J가 제공 그 구현체로 logback 같은 로그 라이브러리를 선택한다.

**LogTest**

~~~java
 private final Logger log = LoggerFactory.getLogger(getClass());
@RequestMapping("/log-test")
     public String logTest() {
String name = "Spring";
         log.trace("trace log={}", name);
         log.debug("debug log={}", name);
         log.info(" info log={}", name);
         log.warn(" warn log={}", name);
         log.error("error log={}", name);
//로그를 사용하지 않아도 a+b 계산 로직이 먼저 실행됨, 이런 방식으로 사용하면 X log.debug("String concat log=" + name);
return "ok";
}
~~~

**매핑 정보**
* @RestController
    * @Controller는 반환 값이 String이면 `View`로 인식한다.
    그래서 뷰가 렌더링
    * @RestController는 반환 값으로 뷰를 찾는게 아니라 **HTTP 메시지 바디에 바로 입력**

**테스트**
* 로그 레벨 설정을 변경할 수 있다.
    * LEVEL : trace > debug > info > Warn > Error
    * 개발 서버는 debug
    * 운영 서버는 info 

**장점**
* 쓰레드 정보, 클래스 이름 같은 부가 정보를 함께 볼 수 있고, 출력 모양 조정가능
* 실무에서는 꼭 로그를 사용

~~~java
@RestController
private Logger log = LoggerFactory.getLogger(getClass());
/**
* 기본 요청
* 둘다 허용 /hello-basic, /hello-basic/
* HTTP 메서드 모두 허용 GET, HEAD, POST, PUT, PATCH, DELETE */
     @RequestMapping("/hello-basic")
     public String helloBasic() {
         log.info("helloBasic");
         return "ok";
     }
~~~

**매핑 정보**
* @RequestMapping
    * /hello-basic url 호출이 오면 메서드가 실행되도록 매핑
    * 대부분의 속성을 배열로 제공


**HTTP 메서드 메핑**
@RequestMapping에 mehtod 속성으로 HTTP 메서드를 지정하지 않으면 HTTP 메서드와 무관하게 호출

**HTTP 메서드 메핑**

~~~java
@RequestMapping(value = "/mapping-get-v1", method = RequestMethod.GET)
 public String mappingGetV1() {
     log.info("mappingGetV1");
     return
 }

 -> @GetMapping(value = "/mapping-get-v2") 
~~~
* Get을 제외한 다른 요청을하면 405반환
* 편안하게 GetMapping으로 축약 가능

**PathVariable 사용**

~~~java
 @GetMapping("/mapping/{userId}")
 public String mappingPath(@PathVariable("userId") String data) {
     log.info("mappingPath userId={}", data);
     return "ok";
 }
 ~~~
 
 최근 HTTP api는 다음과 같이 리소스 경로에 식별자를 넣는 스타일 선호

* /mapping/userA
* /users/1

* @RequestMapping은 URL 경로를 탬플릿화 할 수 잇는데, @PathVariable을 사용하면 매칭 되는 부분을 편리하게 조회할 수 있다.


**특정 파라미터, 헤더, 타입 조건 매핑**
~~~java
1.  @GetMapping(value = "/mapping-param", params = "mode=debug")

2.  @GetMapping(value = "/mapping-header", headers = "mode=debug")
3.  @PostMapping(value = "/mapping-consume", consumes = "application/json")
~~~


## 요청 매핑 - API 예시


~~~java

 @RestController
 @RequestMapping("/mapping/users")
 @GetMapping
    public String users() {
        return "get users";
    }
    /**
     * POST /mapping/users
     */
    @PostMapping
    public String addUser() {
        return "post user";
    }
    /**
     * GET /mapping/users/{userId}
     */
    @GetMapping("/{userId}")
    public String findUser(@PathVariable String userId) {
        return "get userId=" + userId;
    }
    /**
     * PATCH /mapping/users/{userId}
     */
    @PatchMapping("/{userId}")
    public String updateUser(@PathVariable String userId) {
        return "update userId=" + userId;
    }
    /**
     * DELETE /mapping/users/{userId}
     */
    @DeleteMapping("/{userId}")
    public String deleteUser(@PathVariable String userId) {
        return "delete userId=" + userId;
    }
~~~

## 요청 파라미터 - 쿼리 파라미터,html form

**클라이언트에서 서버로 요청 데이터를 전달할 때는 주로 다음 3가지 방법을 사용한다.**
* **GET - 쿼리 파라미터**
    * /url**?username=hello&age=20**
    * 메시지 바디 없이, URL의 쿼리 파라미터에 데이터를 포함해서 전달 
    * 예) 검색, 필터, 페이징등에서 많이 사용하는 방식
* **POST - HTML Form**
    * content-type: application/x-www-form-urlencoded
    * 메시지 바디에 쿼리 파리미터 형식으로 전달 username=hello&age=20 
    * 예) 회원 가입, 상품 주문, HTML Form 사용
* **HTTP message body**에 데이터를 직접 담아서 요청 
    * HTTP API에서 주로 사용, JSON, XML, TEXT 
    * 데이터 형식은 주로 JSON 사용
    * POST, PUT, PATCH

### 요청 파라미터 - 쿼리, HTML
HtppServletRequest의 request.getParmeter()를 사용하려면 다음 두가지 요청 파라미터를 조회할 수 있다.

**GET : 쿼리 파라미터 전송**
`http://localhost:8080/request-param?username=hello&age=20`

**POST, HTML Form 전송**
 POST /request-param ...
 content-type: application/x-www-form-urlencoded
 username=hello&age=20

 -> 둘다 형식이 같으므로 구분없이 조회할 수 있다.
 이것을 간단히 **요청 파라미터(request parameter) 조회**라 한다.

 **RequestParamController**

 ~~~java
  @Controller
 public class RequestParamController {
/**
* 반환 타입이 없으면서 이렇게 응답에 값을 직접 집어넣으면, view 조회X */
     @RequestMapping("/request-param-v1")
     public void requestParamV1(HttpServletRequest request, HttpServletResponse
 response) throws IOException {
         String username = request.getParameter("username");
         int age = Integer.parseInt(request.getParameter("age"));
         log.info("username={}, age={}", username, age);
         response.getWriter().write("ok");
     }
~~~


**request.getParameter()**
여기서는 단순히 HttpServletRequest가 제공하는 방식으로 요청 파라미터를 조회했다.


## HTTP 요청 - @RequestParam

~~~java
 @ResponseBody
 @RequestMapping("/request-param-v2")
 public String requestParamV2(
         @RequestParam("username") String memberName,
         @RequestParam("age") int memberAge) {
     log.info("username={}, age={}", memberName, memberAge);
     return "ok";
 }
~~~
* @RequestParam : 파라미터 이름으로 바인딩
* @ResponseBody : view 조회를 무시하고 HTTP message Body에 직접 해당 내용 입력

**RequestParam**의 name(value) 속성이 파라미터 이름으로 사용

* @RequestParam("username") String memberName -> request.getParmaeter("username")

~~~java
@RequestMapping("/request-param-v3")
 public String requestParamV3(
         @RequestParam String username,
         @RequestParam int age) {
     log.info("username={}, age={}", username, age);
     return "ok";
}
~~~

**파라미터 필수, 기본값**

~~~java
@RequestParam(required = true) String username,
@RequestParam(required = false) Integer age)
@RequestParam(required = true, defaultValue = "guest") String username,
@RequestParam(required = false, defaultValue = "-1") int age) 
~~~

* required,defaultValue를 통해서 설정 및 기본값을 세팅할 수 있다.

* required = true시 Null이 반환대기 때문에 int와 같은 primitve 형을 사용하지 못한다.


## HTTP 요청 - @ModelAttribute

~~~java

 @Data
 public class HelloData {
     private String username;
     private int age;
 }
~~~

실제 개발을 할때 요청 파라미터를 받고 -> 객체 생성-> 객체에 값은 set

하지만 이 과정을 완전히 자동화해주는 @ModelAttribue기능을 제공한다.

~~~java
 @Data
 public class HelloData {
     private String username;
     private int age;
 }
 ~~~
 * 롬복 @Data
    * @Getter, @Setter, @ToString, @EqualsAndHashCode, @RequiredArgsConstructors를 자동으로 적용

~~~java

```java
/**
* @ModelAttribute 사용
* 참고: model.addAttribute(helloData) 코드도 함께 자동 적용됨, 뒤에 model을 설명할 때 자세히
설명
  */
 @ResponseBody
 @RequestMapping("/model-attribute-v1")
 public String modelAttributeV1(@ModelAttribute HelloData helloData) {
     log.info("username={}, age={}", helloData.getUsername(),
 helloData.getAge());
     return "ok";
 }
 ~~~

스프링 MVC는 @ModelAttribute가 있으면 다음을 실행
* HelloData 객체를 생성
* 요청 파라미터의 이름으로 `HelloData` 객체의 프로퍼티를 찾는다.

`프로퍼티`란?

~~~java
 class HelloData {
     getUsername();
     setUsername();
}
~~~ 
이런 코드를 가지고 있으면 프로퍼티를 가지고 있다라고 한다.

~~~java
@RequestMapping("/model-attribute-v2")
 public String modelAttributeV2(HelloData helloData) {
     log.info("username={}, age={}", helloData.getUsername(),
 helloData.getAge());
     return "ok";
 }
 ~~~
* @ModelAttribute를 생략할 수 있다.
* @RequestParam도 생략할 수 있다.

스프링은 해당 생략시 다음과 같은 규칙을 적용한다.
* `String` , `int` , `Integer` 같은 단순 타입 = `@RequestParam`
* 나머지 = `@ModelAttribute` (argument resolver 로 지정해둔 타입 외)

## Http 요청 메시지 - 단순 텍스트

* Http message body에 데이터를 직접 담아서 요청
    * HTTP API에 주로 사용
    * 데이터 형식은 JSON
    * POST,PUT,PATCH


요청 파라미터와 다르게, HTTP 메시지 바디를 통해 데이터가 직접 넘어오는 경우는 `@RequestParam` , `@ModelAttribute` 를 사용할 수 없다

~~~java
@PostMapping("/request-body-string-v2")
 public void requestBodyStringV2(InputStream inputStream, Writer responseWriter)
 throws IOException {
     String messageBody = StreamUtils.copyToString(inputStream,
 StandardCharsets.UTF_8);
     log.info("messageBody={}", messageBody);
     responseWriter.write("ok");
 }
 ~~~
 **스프링 mvc는 다음 파라미터를 지원**

 * InputStream(Reader) : http 요청 메세지 바디의 내용을 직접 조회
 * OutputStream(Writer) : http 응답 메시지의 바디에 직접 결과 출력

 ~~~java
  @PostMapping("/request-body-string-v3")
 public HttpEntity<String> requestBodyStringV3(HttpEntity<String> httpEntity) {
     String messageBody = httpEntity.getBody();
     log.info("messageBody={}", messageBody);
return new HttpEntity<>("ok");
 }
~~~

* **HttpEntity** : header,body 정보를 편리하게 조회
    * 메시지 바디 정보를 직접 조회
    * 요청 파라미터를 조회하는 기능과 관계 x(@RequestParam x, @ModelAttribute x)

* **HttpEntitiy**는 응답에도 사용 가능
    * 메시지 바디 정보 직접 반환

`HttpEntity` 를 상속받은 다음 객체들도 같은 기능을 제공한다.
* **RequestEntity**
    * HttpMethod, url 정보가 추가, 요청에서 사용 
* **ResponseEntity**
    * HTTP 상태 코드 설정 가능, 응답에서 사용
    * `return new ResponseEntity<String>("Hello World", responseHeaders,
HttpStatus.CREATED)`

~~~java
 @PostMapping("/request-body-string-v4")
 public String requestBodyStringV4(@RequestBody String messageBody) {
     log.info("messageBody={}", messageBody);
     return "ok";
 }
~~~
**@RequestBody**
`@RequestBody`를 사용하면 HTTP 메시지 바디 정보를 편리하게 조회할 수 있다.

헤더 정보가 필요하면 HttpEntity, @RequestHeader

이렇게 메시지 바디를 직접 조회하는 기능은 요청 파리미터를 조회하는 @RequestParam, @ModelAttribute와 상관이 ㅇ벗다

**요청 파라미터 vs HTTP 메시지 바디**
* 요청 파라미터 : @RequestParam, @mModelAttribue
* HTTp 메시지 바디를 직접 조회 : @RequestBody

@ResponseBody
응답 결과를 HTTP 메세지 바디에 직접 담아 전달 -> view 사용 x

## HTTP 요청 메시지 -JSON

~~~java
@ResponseBody
 @PostMapping("/request-body-json-v2")
 public String requestBodyJsonV2(@RequestBody String messageBody) throws
 IOException {
     HelloData data = objectMapper.readValue(messageBody, HelloData.class);
     log.info("username={}, age={}", data.getUsername(), data.getAge());
     return "ok";
}

 * @RequestBody를 사용해서 HTTP 메시지 데이터를 꺼내고 messageBody에 저장한다.
 * 문자로 JSON 데이터인 messageBody를 objectMapper를 통해 자바 객체로 변환

 문자로 변환 -> json으로 변환하는 과정이 불편
 @ModelAttribute처럼 한번에 객체로 변환 할수 없나?

~~~java
 @ResponseBody
 @PostMapping("/request-body-json-v3")
 public String requestBodyJsonV3(@RequestBody HelloData data) { log.info("username={}, age={}", data.getUsername(), data.getAge());
     return "ok";
 }
~~~

@RequestBody 객체 차라미터
* @RequestBody HelloData data
* @RequestBody에 직접 만든 객체를 지정 가능

**@RequestBody는 생략 불가능**
`@ModelAttribute` 에서 학습한 내용을 떠올려보자.

생략되면서 @ModelAttribute가 들어와 버린다.


~~~java
 @ResponseBody
 @PostMapping("/request-body-json-v5")
 public HelloData requestBodyJsonV5(@RequestBody HelloData data) {
     log.info("username={}, age={}", data.getUsername(), data.getAge());
     return data;
 }
~~~

@ResponsBody
응답의 경우에도 @ResponseBody를 사용하면 HTTP메시지 바디에 직접 넣어준다.

## 뷰 템플릿
~~~java
 @Controller
 public class ResponseViewController {
     @RequestMapping("/response-view-v1")
     public ModelAndView responseViewV1() {
         ModelAndView mav = new ModelAndView("response/hello")
                 .addObject("data", "hello!");
return mav; }
     @RequestMapping("/response-view-v2")
     public String responseViewV2(Model model) {
         model.addAttribute("data", "hello!!");
         return "response/hello";
     }
@RequestMapping("/response/hello")
     public void responseViewV3(Model model) {
         model.addAttribute("data", "hello!!");
     }
~~~

@ResponseBody가 없으면 response/hello 뷰 리졸버가 뷰를 찾고, 렌더링 한다.
@ResponseBody가 있으면 뷰 리졸버를 시행하지 않고 바로 respons/hello라는 문자가 입력


~~~java

GetMapping("/response-body-string-v2")
     public ResponseEntity<String> responseBodyV2() {
         return new ResponseEntity<>("ok", HttpStatus.OK);
     }
     @ResponseBody
     @GetMapping("/response-body-string-v3")
     public String responseBodyV3() {
         return "ok";
     }
     @GetMapping("/response-body-json-v1")
     public ResponseEntity<HelloData> responseBodyJsonV1() {
         HelloData helloData = new HelloData();
         helloData.setUsername("userA");
         helloData.setAge(20);
         return new ResponseEntity<>(helloData, HttpStatus.OK);
     }
     @ResponseStatus(HttpStatus.OK)
     @ResponseBody
     @GetMapping("/response-body-json-v2")
     public HelloData responseBodyJsonV2() {
         HelloData helloData = new HelloData();
         helloData.setUsername("userA");
         helloData.setAge(20);
         return helloData;
     }
~~~
**@RestController**
`@Controller` 대신에 `@RestController` 애노테이션을 사용하면, 해당 컨트롤러에 모두 `@ResponseBody` 가
적용되는 효과가 있다. 따라서 뷰 템플릿을 사용하는 것이 아니라, HTTP 메시지 바디에 직접 데이터를 입력한다. 이름 그대로 Rest API(HTTP API)를 만들 때 사용하는 컨트롤러이다.
