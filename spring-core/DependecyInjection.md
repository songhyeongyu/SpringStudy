# Dependecy injection methd

1. 생성자 주입
2. 수정자 주입
3. 필드 주입
4. 일반 메서드 주입


## 생성자 주입

**특징**

* 생성자 호출시점에 딱 1번만 호출되는 것이 보장
* 불변, 필수 의존관계에 사용

~~~java
@Component
public class OrderServiceImpl implements OrderService {
    private final MemberRepository memberRepository;
    private final DiscountPolicy discountPolicy;
@Autowired
      public OrderServiceImpl(MemberRepository memberRepository, DiscountPolicy
 discountPolicy) {
         this.memberRepository = memberRepository;
         this.discountPolicy = discountPolicy;
 }
 ~~~

 ❗️생성자가 딱 1개만 있으면 @Autowired를 생략해도 자동 주입된다.

 ## 수정자 주입

 * 선택, 변경 가능성이 있는 의존관계에 사용
 * 자바진 프로퍼티 규약의 수정자 메서드 방식

 ~~~java
 @Autowired
    public void setMemberRepository(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }
     @Autowired
    public void setDiscountPolicy(DiscountPolicy discountPolicy) {
        this.discountPolicy = discountPolicy;
    }
~~~

❗️@Autowired의 기본 동작은 주입할 대상이 없으면 오류가 발생 -> @Autowired(required = false)로 지정하면 된다.

## 필드 주입

**특징**

* 사용x
~~~java
 @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private DiscountPolicy discountPolicy;
~~~
* @SpirngBootTest처럼 스프링 컨테이너를 테스트에 통합한 경우에만 사용

## 일반 메서드 주입

~~~java

    @Autowired
    public void init(MemberRepository memberRepository, DiscountPolicy
  discountPolicy) {
         this.memberRepository = memberRepository;
         this.discountPolicy = discountPolicy;
  }
~~~
❗️ 스프링 빈이 아닌 Member 같은 클래스에 @Autowired코드를 적용해도 아무 기능 하지 않는다.

### 옵션처리

* `@Autowired(required=false)` : 자동 주입할 대상이 없으면 수정자 메서드 자체가 호출 안됨 
* `org.springframework.lang.@Nullable` : 자동 주입할 대상이 없으면 null이 입력된다.
* `Optional<>` : 자동 주입할 대상이 없으면 `Optional.empty` 가 입력된다

❗️@Nullable,Optional은 특정 필드에만 사용

-> 생성자 주입을 선택해라!!

1. 생성자 주입을 사용하면 **컴파일 오류** 발생
2. final 키워드 사용 가능


## lombok

**정리**

최근에는 생성자를 딱 1개 두고, `@Autowired` 를 생략하는 방법을 주로 사용한다. 여기에 Lombok 라이브러리의
`@RequiredArgsConstructor` 함께 사용하면 기능은 다 제공하면서, 코드는 깔끔하게 사용할 수 있다.

~~~java
@Component
 @RequiredArgsConstructor
 public class OrderServiceImpl implements OrderService {
     private final MemberRepository memberRepository;
     private final DiscountPolicy discountPolicy;
 }
~~~

* @RequiredArgsConstructor 기능을 사용하면 final이 붙은 필드를 모아 생성자를 자동으로 만들어 준다!!!


## 조회 빈이 2개 이상

~~~java
@Component
 public class FixDiscountPolicy implements DiscountPolicy {}

 @Component
 public class RateDiscountPolicy implements DiscountPolicy {}
~~~

둘다 스프링 빈으러 선언하면 스프링 빈에서 2개가 발견된다고 오류가 발생한다.

이를 해결하기 위해 3개의 해결방안이 있다

### @Autowired 필드명, @Qualifier, @Primary

1. @Autowired

~~~java
**기존 코드**
 @Autowired
 private DiscountPolicy discountPolicy

**필드 명을 빈 이름으로 변경**
 @Autowired
 private DiscountPolicy rateDiscountPolicy
~~~

2. @Qualifier

~~~java
@Component
 @Qualifier("mainDiscountPolicy")
public class RateDiscountPolicy implements DiscountPolicy {} 

 @Component
 @Qualifier("fixDiscountPolicy")
public class FixDiscountPolicy implements DiscountPolicy {}

@Autowired
 public OrderServiceImpl(MemberRepository memberRepository,
                         @Qualifier("mainDiscountPolicy") DiscountPolicy
 discountPolicy) {
     this.memberRepository = memberRepository;
     this.discountPolicy = discountPolicy;
} -> 생성자 주입시
~~~

**정리**
1. @Qualifier끼리 매칭
2. 빈이름매칭
3. `NoSuchBeanDefinitionException` 예외 발생


3. @Primary
~~~java
@Component
 @Primary
 public class RateDiscountPolicy implements DiscountPolicy {}
 @Component
 public class FixDiscountPolicy implements DiscountPolicy {}
~~~

어떤 걸 사용할까?
메인 데이터베이스의 커넥션을 획득하는 스프링 빈은 @Primary를 적용

서브 데이터베이스 커넥션 빈을 획득할 때는 @Qualifie를 지정 -> 하지만 상관 없다.

### 조회한 빈 모두 조회 List,map

~~~java
 @Test
    void findAllBean() {
        ApplicationContext ac = new
AnnotationConfigApplicationContext(AutoAppConfig.class, DiscountService.class);
        DiscountService discountService = ac.getBean(DiscountService.class);
        Member member = new Member(1L, "userA", Grade.VIP);
        int discountPrice = discountService.discount(member, 10000,
"fixDiscountPolicy");
        assertThat(discountService).isInstanceOf(DiscountService.class);
        assertThat(discountPrice).isEqualTo(1000);
    }
    static class DiscountService {
        private final Map<String, DiscountPolicy> policyMap;
        private final List<DiscountPolicy> policies;
        public DiscountService(Map<String, DiscountPolicy> policyMap,
List<DiscountPolicy> policies) {
            this.policyMap = policyMap;
            this.policies = policies;
            System.out.println("policyMap = " + policyMap);
            System.out.println("policies = " + policies);
        }
        public int discount(Member member, int price, String discountCode) {
            DiscountPolicy discountPolicy = policyMap.get(discountCode);
            System.out.println("discountCode = " + discountCode);
            System.out.println("discountPolicy = " + discountPolicy);
            return discountPolicy.discount(member, price);
} }
~~~

-> 클라이언트가 할인의 종류를 선택할 수 있다고 가정

* Map은 모든 DiscountPolicy 주입
* discount () 메서드는 fixDiscountPolicy가 넘어오면 map에서 fix 실행, rate가 넘어오면 rate실행


참고로 **스프링과 스프링 부트가 자동으로 등록하는 수 많은 빈들은 예외**다. 이런 부분들은 스프링 자체를 잘 이해하고 스프링의 의도대로 잘 사용하는게 중요하다. 스프링 부트의 경우 `DataSource` 같은 데이터베이스 연결에 사용하는 기 술 지원 로직까지 내부에서 자동으로 등록하는데, 이런 부분은 메뉴얼을 잘 참고해서 스프링 부트가 의도한 대로 편리하 게 사용하면 된다. 반면에 **스프링 부트가 아니라 내가 직접 기술 지원 객체를 스프링 빈으로 등록한다면 수동으로 등록해 서 명확하게 드러내는 것이 좋다.**


**정리**
편리한 자동 기능은 기본
직접 등록하는 기술 지원 객체는 수동
다형성은 수동 등록 고민