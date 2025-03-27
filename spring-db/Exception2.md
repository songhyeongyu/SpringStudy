# check Exception and Interface

서비스 계층은 가급적 특정 구현 기술 의존 x
예외에 대한 의존도 함께 해결


## interface

**인터페이스 도입 그림**

![alt text](image-41.png)

* 이렇게 인터페이스를 도입하면 `MemberService`는 `MemberRepository`에만 의존하면 된다.

**MemberRepository**

~~~java
public interface MemberRepository {
      Member save(Member member) throws SQLException;
      Member findById(String memberId);
      void update(String memberId, int money);
      void delete(String memberId);
}
~~~

SQLException이 체크 예외기 때문에 예외를 선언해야된다.

**체크 예외 코드**

~~~java
public Member save(Member member) throws SQLException {
          String sql = "insert into member(member_id, money) values(?, ?)";
}
~~~

* 인터페이스의 구현체가 체크 예외를 던지려면 인터페이스 메서드에 먼저 예외를 던지는 부분이 선언 되어 있어야 한다.
      * 쉽게 이야기 해서 `MemberRepositoryV3` 가 `throws SQLException` 를 하려면 `MemberRepositoryEx` 인터페이스에도 `throws SQLException` 이 필요하다.

**런타임 예외와 인터페이스**
런타임 예외는 이런 부분에서 자유롭다 -> 특정 기술에 종속적일 필요가 없다.

## 런타임 예외 적용

~~~java
  public interface MemberRepository {
      Member save(Member member);
      Member findById(String memberId);
      void update(String memberId, int money);
      void delete(String memberId);
}
~~~


**MyDbException**
~~~java
 public class MyDbException extends RuntimeException {
      public MyDbException() {
      }
      public MyDbException(String message) {
          super(message);
}
      public MyDbException(String message, Throwable cause) {
          super(message, cause);
}
  public MyDbException(Throwable cause) {
          super(cause);
  }
 }
~~~

* RuntimeException을 상속받아 MyDbException은 언체크 예외로 바뀐다.


~~~java
  @Override
      public Member save(Member member) {
          String sql = "insert into member(member_id, money) values(?, ?)";
          Connection con = null;
          PreparedStatement pstmt = null;
  try {
              con = getConnection();
              pstmt = con.prepareStatement(sql);
            pstmt.setString(1, member.getMemberId());
            pstmt.setInt(2, member.getMoney());
            pstmt.executeUpdate();
            return member;
        } catch (SQLException e) {
            throw new MyDbException(e);
        } finally {
            close(con, pstmt, null);
        }
}
~~~

* MemberRepository 인터페이스를 구현
* SQLException -> MyDbException이라는 런타임 예외로 변환해서 던지는 부분이다.

**예외 변환**

~~~java
catch (SQLException e) {
      throw new MyDbException(e);
}
~~~

* 예외는 원인이 ㄷ되는 예외를 내부에 포함할 수 있는데 이렇게 작성해야 된다.
* MyDbException이 내부에 SQLException이 있다.

~~~java
catch (SQLException e) {
      throw new MyDbException();
}
~~~

* 이런 식으로 절대 작성하면 안된다.

~~~java
@RequiredArgsConstructor
  public class MemberServiceV4 {
      private final MemberRepository memberRepository;
      @Transactional
      public void accountTransfer(String fromId, String toId, int money) {
          bizLogic(fromId, toId, money);
      }
        private void bizLogic(String fromId, String toId, int money) {
        Member fromMember = memberRepository.findById(fromId);
        Member toMember = memberRepository.findById(toId);
        memberRepository.update(fromId, fromMember.getMoney() - money);
        validation(toMember);
        memberRepository.update(toId, toMember.getMoney() + money);
}
    private void validation(Member toMember) {
        if (toMember.getMemberId().equals("ex")) {
throw new IllegalStateException("이체중 예외 발생"); }
}
~~~
* MemberRepository 인터페이스에 의존하도록 변경
* 순수 서비스 완성


## 스프링 예외 추상화

![alt text](image-42.png)

* `DataAccessException`은 2가지로 구분되는데 `NonTrasient` 예외와 `Transient`예외이다.


~~~java
 @Test
  void exceptionTranslator() {
      String sql = "select bad grammar";
      try {
          Connection con = dataSource.getConnection();
          PreparedStatement stmt = con.prepareStatement(sql);
          stmt.executeQuery();
      } catch (SQLException e) {
          assertThat(e.getErrorCode()).isEqualTo(42122);
          //org.springframework.jdbc.support.sql-error-codes.xml
          SQLExceptionTranslator exTranslator = new
  SQLErrorCodeSQLExceptionTranslator(dataSource);
          //org.springframework.jdbc.BadSqlGrammarException
          DataAccessException resultEx = exTranslator.translate("select", sql,
  e);
          log.info("resultEx", resultEx);
  assertThat(resultEx.getClass()).isEqualTo(BadSqlGrammarException.class);
      }
}
~~~

~~~java
SQLExceptionTranslator exTranslator = new
  SQLErrorCodeSQLExceptionTranslator(dataSource);
  DataAccessException resultEx = exTranslator.translate("select", sql, e);
~~~

* 스프링이 제공하는 SQL 예외 변환기는 다음과 같이 사용

* `translate()` 메서드의 첫번째 파라미터는 읽을 수 있는 설명이고, 두번째는 실행한 sql, 마지막은 발생된
`SQLException` 을 전달하면 된다. 이렇게 하면 적절한 스프링 데이터 접근 계층의 예외로 변환해서 반환해준다.

**정리**

* 스프링은 데이터 접근 계층에 일관된 예외 추상화 제공


## 스프링 예외 추상화 적용

~~~java


private final DataSource dataSource;
private final SQLExceptionTranslator exTranslator;
public MemberRepositoryV4_2(DataSource dataSource) {
          this.dataSource = dataSource;
          this.exTranslator = new
  SQLErrorCodeSQLExceptionTranslator(dataSource);
  }

@Override
public Member save(Member member) {
    String sql = "insert into member(member_id, money) values(?, ?)";
    Connection con = null;
    PreparedStatement pstmt = null;
    try {
        con = getConnection();
        pstmt = con.prepareStatement(sql);
        pstmt.setString(1, member.getMemberId());
        pstmt.setInt(2, member.getMoney());
        pstmt.executeUpdate();
        return member;
    } catch (SQLException e) {
        throw exTranslator.translate("save", sql, e);
    } finally {
        close(con, pstmt, null);
} }
~~~

~~~java
catch (SQLException e) {
      throw exTranslator.translate("save", sql, e);
}
~~~
기존 코드에서 스프링 예외 변환기 사용


## jdbc 반복 문제 해결

~~~java
  @Override
    public Member save(Member member) {
        String sql = "insert into member(member_id, money) values(?, ?)";
        template.update(sql, member.getMemberId(), member.getMoney());
        return member;
}
~~~

* `JdbcTemplate` 은 JDBC로 개발할 때 발생하는 반복을 대부분 해결해준다. 그 뿐만 아니라 지금까지 학습했던, **트랜 잭션을 위한 커넥션 동기화**는 물론이고, 예외 발생시 **스프링 예외 변환기**도 자동으로 실행해준다.
