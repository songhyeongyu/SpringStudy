# component scan and autoconfig

* 지금까지 스프링 빈을 등록할 때 직접 @Bean이나 xml을 통해서 설정 정보를 등록했다.
* 컴포넌트 스캔이라는 기능을 제공
* 이러한 것들을 DI 하기 위해 @AutoWired 제공

~~~java
@Configuration
 @ComponentScan(
         excludeFilters = @Filter(type = FilterType.ANNOTATION, classes =
 Configuration.class))
 public class AutoAppConfig {
 }
~~~

* 컴포넌트 스캔을 사용하려면 @ComponentScan을 설정 정보에 붙여주면 된다.
* 기존의 AppConfig와 다르게 @Bean으로 등록한 클래스가 없다.

**@Component** 애노테이션이 붙은 클래스를 스캔하면 스프링 빈으로 등록한다. @Component를 붙여주자

~~~java
 @Component
 public class MemoryMemberRepository implements MemberRepository {}

 @Component
 public class RateDiscountPolicy implements DiscountPolicy {}

  @Component
 public class MemberServiceImpl implements MemberService {
     private final MemberRepository memberRepository;
     @Autowired
     public MemberServiceImpl(MemberRepository memberRepository) {
         this.memberRepository = memberRepository;
     }

@Autowired
     public OrderServiceImpl(MemberRepository memberRepository, DiscountPolicy
 discountPolicy) {
         this.memberRepository = memberRepository;
         this.discountPolicy = discountPolicy;
     }
 }
 ~~~
 * @Autowired는 의존관계 자동 주입
 * @Autowired를 사용하면 생성자에서 여러 의존관계도 한번에 주입할 수 있다.

* basePackageClasses: 지정한 클래스의 패키지를 탐색 시작 위치로 지정한다.


 **권장하는 방법**
개인적으로 즐겨 사용하는 방법은 패키지 위치를 지정하지 않고, 설정 정보 클래스의 위치를 프로젝트 최상단에 두는 것 이다. 최근 스프링 부트도 이 방법을 기본으로 제공한다.

#### 수동 빈 vs 자동 빈

수동빈이 우선권을 갖지만, 어려운 버그가 만들어져 스프링 부트에서는 에러가 나버린다.


