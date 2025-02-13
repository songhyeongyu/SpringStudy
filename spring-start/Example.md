# 예제로 코드의 동작을 확인해 본다.

일반적인 웹 애플리케이션 계층 구조

컨트롤러 → 서비스 → 리포지토리 → DB
    ↘      ↓     ↙ 
          도메인  


* 서비스 : 비즈니스 로직 구현
* 리포지토리 : 도메인 객체를 db에 저장하고 관리
* 도메인 : 비즈니스 도메인 객체

MemberService →MemberRepository(Interface)
                   ↑
                   │ (implements)
                   │
MemoryMemberRepository

* 가벼운 메모리 기반의 데이터 저장소를 사용한다.

~~~java
MEMBER객체

public class Member {
     private Long id;
     private String name;
     public Long getId() {
         return id;
}
     public void setId(Long id) {
         this.id = id;
}
  public String getName() {
         return name;
}
     public void setName(String name) {
         this.name = name;
} }
~~~

**레포지토리 인터페이스**
~~~java
Member save(Member member);
     Optional<Member> findById(Long id);
     Optional<Member> findByName(String name);
     List<Member> findAll();
~~~

## 테스트 케이스 작성

개발한 기능을 실행할 땐, 테스트를 해봐야 되기 때문에 테스트 케이스를 만들어 구현해봐야 된다. 하지만 이런 방법은 오래 걸리고 실행하기 어렵고 여러 테스트를 한 번에 실행하기 어렵다는 단점이 존재한다.
따라서 자바는 JUNIT이라는 프레임워크로 테스트 한다.


~~~java
@AfterEach
public void afterEach() {
    repository.clearStore();
}
@Test
    public void findAll() {
//given
        Member member1 = new Member();
        member1.setName("spring1");
        repository.save(member1);
        Member member2 = new Member();
        member2.setName("spring2");
        repository.save(member2);
//when
        List<Member> result = repository.findAll();
//then
        assertThat(result.size()).isEqualTo(2);
    }
@AfterEach
public void afterEach() {
    repository.clearStore();
}
~~~
* 기본적인 테스트 코드 작성하는 방법이다.
@Test 어노테이션을 붙여서 사용한다.
* @AfterEach: 한 번에 여러 테스트를 실행하면 메모리 DB에 직전 테스트의 결과가 남을 수도 있다. 이럴 때 @AfterEach를 사용해서 메모리에 남아있는 데이터들을 삭제한다.

### 회원 서비스 테스트

**회원 서비스 코드를 DI 가능하게 변경한다.**

~~~java
 public class MemberService {
     private final MemberRepository memberRepository;
     public MemberService(MemberRepository memberRepository) {
         this.memberRepository = memberRepository;
}
 }

  @BeforeEach
     public void beforeEach() {
         memberRepository = new MemoryMemberRepository();
         memberService = new MemberService(memberRepository);
     }
~~~
* @BeforeEach : 각 테스트 실행 전에 테스트가 서로 영향이 없도록 항상 새로운 객체를 생성, 의존관계도 맺어준다.



