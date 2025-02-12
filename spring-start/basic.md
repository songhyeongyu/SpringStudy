# MVC

* MVC : model, view, controller

~~~java
 @GetMapping("hello-mvc")
     public String helloMvc(@RequestParam("name") String name, Model model) {
         model.addAttribute("name", name);
         return "hello-template";
     }
~~~

return이 hello-template이면 viewResolver가 Thymeleaf템플릿 엔진으로 처리해서 Html로 변환시켜준다.

## API

~~~java

     @GetMapping("hello-string")
     @ResponseBody
     public String helloString(@RequestParam("name") String name) {
         return "hello " + name;
     }
~~~
**@ResponsBody 문자반환**

* `ResponsBody`를 사용하면 viewResolver를 사용하지 않는다.
* HTTP의 BODY에 문자 내용을 직접 반환한다.
* `@ResponseBody` 를 사용하고, 객체를 반환하면 객체가 JSON으로 변환됨

**정리**

@ResponseBody를 사용하면
 * HTTP의 BODY에 문자 내용을 직접 반환
 * HttpMessageConverter가 동작
 