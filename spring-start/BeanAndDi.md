# Set component and di

~~~java
 @Controller
 public class MemberController {
     private final MemberService memberService;
     @Autowired
     public MemberController(MemberService memberService) {
         this.}
}
~~~
* @AutoWired가 있으면 스프링이 연관된 객체를 스프링 컨테이너에서 찾아서 넣어준다. -> DI(dependenc Injection) 의존성 주입이라고 한다.

**스프링 빈을 등록하는 2가지 방법**

* 컴포넌트 스캔과 자동 의존관계 - 위에서 보여준 방법
* 자바 코드로 직접

## component

* @Component가 있으면 스프링 빈으로 자동 등록
    * @Controller
    * @Service
    * @Repository

@Autowired를 사용하면 객체 생성 시점에서 스프링 컨테이너에서 해당 스프링 빈을 찾아 주입

**스프링은 스프링 컨테이너에 스프링 빈을 등록할 때, 기본으로 싱글톤으로 등록 -> 같은 스프링 빈이면 모두 같은 인스턴스**

~~~java
 @Configuration
 public class SpringConfig {
     @Bean
     public MemberService memberService() {
         return new MemberService(memberRepository());
     }
     @Bean
     public MemberRepository memberRepository() {
         return new MemoryMemberRepository();
     }
 }
~~~
**자바 코드로 스프링 빈 설정 방법**

* `@Autowired` 를 통한 DI는 `helloController` , `memberService` 등과 같이 스프링이 관리하 는 객체에서만 동작한다. 

