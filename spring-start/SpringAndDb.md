# JDBC
과거에 자바와 db를 연동하려면 이렇게 해야됐다.

~~~java
@Override
public Member save(Member member) {
    String sql = "insert into member(name) values(?)";
    Connection conn = null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    try {
        conn = getConnection();
        pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        pstmt.setString(1, member.getName());
        pstmt.executeUpdate();
        rs = pstmt.getGeneratedKeys();
        if (rs.next()) {
            member.setId(rs.getLong(1));
} else {
throw new SQLException("id 조회 실패");
}
        return member;
    } catch (Exception e) {
        throw new IllegalStateException(e);
    } finally {
        close(conn, pstmt, rs);
    }
}
~~~

**spirng configuer 변경**
~~~java
 @Bean
    public MemberRepository memberRepository() {
        // return new MemoryMemberRepository();
        return new JdbcMemberRepository(dataSource);
    }
~~~
memory에서 관리하는 것이 아닌 db를 연동해서 관리하는 방식이다.

하지만 코드의 길이가 길고 객체지향적이 아닌 db테이블에 맞춰서 설계를 해야 됐다.

* 개방-폐쇄 원칙(OCP, Open-Closed Principle) 확장에는 열려있고, 수정, 변경에는 닫혀있다.

* 스프링의 DI (Dependencies Injection)을 사용하면 **기존 코드를 전혀 손대지 않고, 설정만으로 구현 클 래스를 변경**할 수 있다.


# JPA

위에 같은 코드를 단순화 하기위해 jpa라는 기술이 도입되기 시작했다.

~~~

resources/application.properties

 spring.datasource.url=jdbc:h2:tcp://localhost/~/test
 spring.datasource.driver-class-name=org.h2.Driver
 spring.datasource.username=sa
 spring.jpa.show-sql=true
 spring.jpa.hibernate.ddl-auto=none
 ~~~

* `show-sql` : JPA가 생성하는 SQL을 출력한다.
* `ddl-auto` : JPA는 테이블을 자동으로 생성하는 기능을 제공하는데 `none` 를 사용하면 해당 기능을 끈다. `create` 를 사용하면 엔티티 정보를 바탕으로 테이블도 직접 생성해준다. 해보자.

~~~java
 @Entity
 public class Member {
     @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
     private Long id;
     private String name;
 }

 private final EntityManager em;
     public JpaMemberRepository(EntityManager em) {
         this.em = em;
}
     public Member save(Member member) {
         em.persist(member);
         return member;
}

 @Transactional
public class MemberService {}



    private final EntityManager em;
@Bean
public MemberRepository memberRepository() {
    return new JpaMemberRepository(em);
}
~~~
테이블을 위한 설계가 아닌 개발자가 설계한 세상인 객체 지향적으로 설계를 할 수 있고 가독성이 훨씬 좋아진 코드를 볼 수 있다.

* 스프링은 해당 클래스의 메서드를 실행할 때 트랜잭션을 시작하고, 메서드가 정상 종료되면 트랜잭션을 커 밋한다. 만약 런타임 예외가 발생하면 롤백한다.

* **JPA를 통한 모든 데이터 변경은 트랜잭션 안에서 실행해야 한다.** -> 대부분 service class에 선언한다.



