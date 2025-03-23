# Spring Type Converter

~~~java
package hello.typeconverter.controller;
 import org.springframework.web.bind.annotation.GetMapping;
 import org.springframework.web.bind.annotation.RestController;
 import javax.servlet.http.HttpServletRequest;
 @RestController
 public class HelloController {
     @GetMapping("/hello-v1")
     public String helloV1(HttpServletRequest request) {
String data = request.getParameter("data"); //문자 타입 조회 Integer intValue = Integer.valueOf(data); //숫자 타입으로 변경 System.out.println("intValue = " + intValue);
return "ok";
} }
~~~
**HelloController- 문자를 숫자로 변경**

`String data = request.getParameter("data")` : Http 요청 파라미터는 모두 문자로 처리, 요청 파라미터를 자바에서 다른 타입으로 변환해서 사용하려면 이렇게 변환해야 된다.

Integer intValue = Integer.valueOf(data)

이번엔 스프링 MVC가 제공하는 @RequestParam

~~~java
 @GetMapping("/hello-v2")
 public String helloV2(@RequestParam Integer data) {
     System.out.println("data = " + data);
     return "ok";
 }
~~~

@RequestParam을 이용하면 Integer 타입의 숫자 10으로 편하게 받을 수 있다.

ex) 
`@ModelAtribute`
~~~java
 @ModelAttribute UserData data
 class UserData {
   Integer data;
}
~~~

`@PathVariable`
~~~java
 /users/{userId}
 @PathVariable("userId") Integer data
~~~

스프링 타입 변환 적용

* 스프링 MVC 요청 파라미터
    * @RequestParam, @ModelAttribute, @ParhVariable
* @Value등으로 YML 정보
* XML에 넣은 스프링 빈 정보를 변환
* 뷰를 렌더링 할 때

**컨버터 인터페이스**
컨버터 인터페이스를 사용해서 새로운 타입을 만들거나, Boolean 타입을 숫자로 변경하는 것이 가능하다.

**사용자 정의 타입 컨버터**

**IpPort**

~~~java
package hello.typeconverter.type;
 import lombok.EqualsAndHashCode;
 import lombok.Getter;
 @Getter
 @EqualsAndHashCode
 public class IpPort {
     private String ip;
     private int port;
     public IpPort(String ip, int port) {
         this.ip = ip;
         this.port = port;
     }
}
~~~

**StringToIpPortConvertr**
~~~java
package hello.typeconverter.converter;
 import hello.typeconverter.type.IpPort;
 import lombok.extern.slf4j.Slf4j;
 import org.springframework.core.convert.converter.Converter;
 @Slf4j
 public class StringToIpPortConverter implements Converter<String, IpPort> {
      @Override
     public IpPort convert(String source) {
         log.info("convert source={}", source);
         String[] split = source.split(":");
         String ip = split[0];
         int port = Integer.parseInt(split[1]);
         return new IpPort(ip, port);
     }
}
~~~

**IpPortToStringConverter**
~~~java
 @Slf4j
 public class IpPortToStringConverter implements Converter<IpPort, String> {
     @Override
     public String convert(IpPort source) {
         log.info("convert source={}", source);
         return source.getIp() + ":" + source.getPort();
     }
}
~~~

**Converter test**
~~~java
 @Test
 void stringToIpPort() {
     StringToIpPortConverter converter = new StringToIpPortConverter();
     String source = "127.0.0.1:8080";
     IpPort result = converter.convert(source);
     assertThat(result).isEqualTo(new IpPort("127.0.0.1", 8080));
}
@Test
 void ipPortToString() {
     IpPortToStringConverter converter = new IpPortToStringConverter();
     IpPort source = new IpPort("127.0.0.1", 8080);
     String result = converter.convert(source);
     assertThat(result).isEqualTo("127.0.0.1:8080");
}
~~~

타입 컨버터를 하나하나 적용하면 개발자가 직접 컨버팅 하는 것과 큰 차이가 없다.

## ConversionService

**ConversionService 인터페이서**

~~~java
public interface ConversionService {
     boolean canConvert(@Nullable Class<?> sourceType, Class<?> targetType);
     boolean canConvert(@Nullable TypeDescriptor sourceType, TypeDescriptor
 targetType);
     <T> T convert(@Nullable Object source, Class<T> targetType);
     Object convert(@Nullable Object source, @Nullable TypeDescriptor sourceType,
 TypeDescriptor targetType);
}

//그리고 사용

@Test
void conversionService() {
DefaultConversionService conversionService = new
 DefaultConversionService();

 conversionService.addConverter(new StringToIpPortConverter());
conversionService.addConverter(new IpPortToStringConverter());
  IpPort ipPort = conversionService.convert("127.0.0.1:8080",
 IpPort.class);
         assertThat(ipPort).isEqualTo(new IpPort("127.0.0.1", 8080));
         String ipPortString = conversionService.convert(new IpPort("127.0.0.1",
 8080), String.class);
         assertThat(ipPortString).isEqualTo("127.0.0.1:8080");
}
~~~

**등록과 사용분리**

컨버터를 등록할 때 타입 컨버터를 명확하게 알아야 하지만 컨버터를 사용하는 입장에서는 몰라도 된다.

따라서 타입 변환을 원하는 사용자는 컨버전 서비스 인터페이스에만 의존하면 된다. 컨버전 서비스를 등록하는 부분과 사용하는 부분을 분리하고 의존관계 주입을 사용해야 된다.

**HelloController**
~~~java
 @GetMapping("/ip-port")
 public String ipPort(@RequestParam IpPort ipPort) {
     System.out.println("ipPort IP = " + ipPort.getIp());
     System.out.println("ipPort PORT = " + ipPort.getPort());
     return "ok";
}
~~~

**처리 과정**
@RequestParam은 ArgumentReolsve인 `RequestParamMethodArgumentResolver`에서  `ConversionService` 를 사용해서 타입을 변환한다. 

## view template에서 사용해가ㅣ

~~~java
@Controller
 public class ConverterController {
     @GetMapping("/converter-view")
     public String converterView(Model model) {
         model.addAttribute("number", 10000);
         model.addAttribute("ipPort", new IpPort("127.0.0.1", 8080));
             return "converter-view";
     }
 }

 @GetMapping("/converter/edit")
   public String converterForm(Model model) {
        IpPort ipPort = new IpPort("127.0.0.1", 8080);
        Form form = new Form(ipPort);
        model.addAttribute("form", form);
        return "converter-form";
    }

    @PostMapping("/converter/edit")
    public String converterEdit(@ModelAttribute Form form, Model model) {
        IpPort ipPort = form.getIpPort();
        model.addAttribute("ipPort", ipPort);
        return "converter-view";
}
    @Data
    static class Form {
        private IpPort ipPort;
        public Form(IpPort ipPort) {
            this.ipPort = ipPort;
} }
~~~
`Model`에 숫자 `10000`와 `ipPort`객체를 담아서 뷰 템플릿에 전다.
~~~html
<form th:object="${form}" th:method="post">
th:field <input type="text" th:field="*{ipPort}"><br/>
th:value <input type="text" th:value="*{ipPort}">(보여주기 용도)<br/> <input type="submit"/>
~~~
* Get
    * th:field가 자동으로 컨버젼 서비스 적용 -> ${{ipPort}}처럼 적용
* Post
    * @ModelAttribute를 사용 String->IpPort로 변환

## formatter

ex)
Integer -> String 출력 시점에 숫자 1000-> 문자 1,000 이렇게 1000단위에 쉼표를 넣어서 출력하거나 또는 1,000라는 문자를 1000으로 변경

~~~java
  @Override
     public Number parse(String text, Locale locale) throws ParseException {
         log.info("text={}, locale={}", text, locale);
         NumberFormat format = NumberFormat.getInstance(locale);
         return format.parse(text);
}
     @Override
     public String print(Number object, Locale locale) {
         log.info("object={}, locale={}", object, locale);
         return NumberFormat.getInstance(locale).format(object);
     }
~~~
"1,000"처럼 숫자 중간의 쉼표를 적용하려면 자바가 기본으로 제공하는 NumberFormat 객체를 사용하면 된다.

parse()를 사용해서 문자 -> 숫자

### support to formater

컨버전 서비스에는 컨버터만 등록, 포맷터를 등록할 수 는 없다.

~~~java
//포맷터 등록
conversionService.addFormatter(new MyNumberFormatter());
//컨버터 사용
assertThat(conversionService.convert(1000,
 String.class)).isEqualTo("1,000");
         assertThat(conversionService.convert("1,000",
 Long.class)).isEqualTo(1000L);
~~~

.addFormatter를 이용해서 사용할 수 있다.

### 스프링이 제공하는 formatter

~~~java
@Controller
 public class FormatterController {
     @GetMapping("/formatter/edit")
     public String formatterForm(Model model) {
         Form form = new Form();
         form.setNumber(10000);
         form.setLocalDateTime(LocalDateTime.now());
         model.addAttribute("form", form);
         return "formatter-form";
     }
     @PostMapping("/formatter/edit")
     public String formatterEdit(@ModelAttribute Form form) {
         return "formatter-view";
     }
     @Data
     static class Form {
         @NumberFormat(pattern = "###,###")
         private Integer number;
           @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime localDateTime;
    }
~~~

**정리**
컨버터, 포매터 사용하는 방법은 다르지만 컨버전 서비스를 통해서 사용할 수 있다.

