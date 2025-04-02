# Transaction 방식

* 선언적 트랜잭션 관리
    * `@Transactional` 애노테이션 하나만 선언해서 관리

* 프로그래밍 방식의 트랜잭션 관리

* 선언적 방식이 99.9%


**선언적 트랜잭션과 AOP**

![alt text](image-7.png)

**서비스 계층의 트랜잭션 사용 코드**

~~~java
 TransactionStatus status = transactionManager.getTransaction(new
 DefaultTransactionDefinition());
try {
//비즈니스 로직
     bizLogic(fromId, toId, money);
transactionManager.commit(status); //성공시 커밋 } catch (Exception e) {
transactionManager.rollback(status); //실패시 롤백
     throw new IllegalStateException(e);
 }
~~~
**프록시 도입 후**

![alt text](image-8.png)

트랜잭션을 처리하기 위한 프록시를 적용하면 트랜잭션을 처리하는 객체외 비즈니스 로직을 처리하는 서비스 객체를 명확하게 분리할 수 있다.

![alt text](image-9.png)

**스프링이 제공하는 트랜잭션 AOP**
* 스프링은 트랜잭션 aop를 처리하기 위한 기능 제공
* 스프링이 @Transactional 애노테이션만 있으면 인식해서 트랜잭션을 알아서 처리함


## 트랜잭션 적용 확인

~~~java
 @Autowired
    BasicService basicService;
    @Test
    void proxyCheck() {
        //BasicService$$EnhancerBySpringCGLIB...
        log.info("aop class={}", basicService.getClass());
        assertThat(AopUtils.isAopProxy(basicService)).isTrue();
    }
    @Test
    void txTest() {
        basicService.tx();
        basicService.nonTx();
    }
    @TestConfiguration
    static class TxApplyBasicConfig {
        @Bean
        BasicService basicService() {
            return new BasicService();
        }
} @Slf4j
    static class BasicService {
        @Transactional
        public void tx() {
            log.info("call tx");
            boolean txActive =
TransactionSynchronizationManager.isActualTransactionActive();
log.info("tx active={}", txActive);}
         public void nonTx() {
             log.info("call nonTx");
             boolean txActive =
 TransactionSynchronizationManager.isActualTransactionActive();
             log.info("tx active={}", txActive);
} }
~~~

**proxyCheck() 실행**

![alt text](image-10.png)

* @Transactional 애노테이션이 특정 클래스나 메서드에 하나라도 있으면 트랜잭션 aop는 프록시를 만들어서 스프링 컨테이너에 등록

* 클라이언트인 txBasicTest는 스프링 컨에티너에 @Autowired로 의존 관계 주입을 요청

* 프록시는 BasicService를 상속해서 만들어지기 때문에 다형성을 활용할 수 있다.

![alt text](image-11.png)


## 주의사항 - 내부호출

![alt text](image-12.png)

* AOP를 적용하면 스프링은 대상 객체 대신에 프록시를 스프링 빈으로 등록 -> 하지만 **대상 객체의 내부에서 메서드 호출이 발생하면 프록시를 거치지 않고 대상 객체를 직접 호출하는 문제가 있다**

코드로 확인해보자!!

~~~java
  @Autowired
     CallService callService;
@Test  void printProxy() {
        log.info("callService class={}", callService.getClass());
}
    @Test
    void internalCall() {
        callService.internal();
    }
    @Test
    void externalCall() {
        callService.external();
    }
    @TestConfiguration
    static class InternalCallV1Config {
        @Bean
        CallService callService() {
            return new CallService();
        }
} @Slf4j
    static class CallService {
        public void external() {
            log.info("call external");
            printTxInfo();
            internal();
}
        @Transactional
        public void internal() {
            log.info("call internal");
            printTxInfo();
        }
        private void printTxInfo() {
            boolean txActive =
TransactionSynchronizationManager.isActualTransactionActive();
            log.info("tx active={}", txActive);
} }
~~~

~~~java
 public void external() {
     log.info("call external");
     printTxInfo();
     internal();
}
~~~

* External은 @Transactional 애노테이션 x
-> 트랜잭션없이 시작

* 내부에 @Transaction이 있는 internal을 호출하는 것을 확인할 수 있다.

* 이 경우는 external은 트랜잭션이 없지만, internal에서는 트랜잭션이 적용되는 것 처럼 보이낟.

* 실행 로그를 보면 트랜잭션 관련 코드가 전혀 보이지 않는다. 프록시가 아닌 실제 `callService` 에서 남긴 로그만 확 인된다. 추가로 `internal()` 내부에서 호출한 `tx active=false` 로그를 통해 확실히 트랜잭션이 수행되지 않은 것을 확인할 수 있다.
우리의 기대와 다르게 `internal()` 에서 트랜잭션이 전혀 적용되지 않았다. 

![alt text](image-13.png)

1. 클라이언트인 테스트 코드는 `callService.external()` 을 호출한다. 여기서 `callService` 는 트랜 잭션 프록시이다.

2. `callService` 의 트랜잭션 프록시가 호출된다.

3. `external()` 메서드에는 `@Transactional` 이 없다. 따라서 트랜잭션 프록시는 트랜잭션을 적용하지 않는다.

4. 트랜잭션 적용하지 않고, 실제 `callService` 객체 인스턴스의 `external()` 을 호출한다.

5. `external()` 은 내부에서 `internal()` 메서드를 호출한다. 그런데 여기서 문제가 발생한다.


**문제 원인**
자바 언어에서 메서드 앞에 별도의 참조가 없으면 `this` 라는 뜻으로 자기 자신의 인스턴스를 가리킨다.
결과적으로 자기 자신의 내부 메서드를 호출하는 `this.internal()` 이 되는데, 여기서 `this` 는 자기 자신을 가리키 므로, 실제 대상 객체( `target` )의 인스턴스를 뜻한다. 결과적으로 이러한 내부 호출은 프록시를 거치지 않는다. 따라서 트랜잭션을 적용할 수 없다. 결과적으로 `target` 에 있는 `internal()` 을 직접 호출하게 된 것이다.

## 문제해결
별도의 클래스르 분리하면 된다.

~~~java

  @Test
    void externalCallV2() {
        callService.external();
    }
        public void external() {
            log.info("call external");
            printTxInfo();
            internalService.internal();
}

   @Transactional
        public void internal() {
            log.info("call internal");
            printTxInfo();
        }
        private void printTxInfo() {
            boolean txActive =
TransactionSynchronizationManager.isActualTransactionActive();
            log.info("tx active={}", txActive);
} }
~~~
## 롤백 기본

**rollbackFor**

~~~java
 @Autowired
     RollbackService service;
     @Test
     void runtimeException() {
         assertThatThrownBy(() -> service.runtimeException())
                 .isInstanceOf(RuntimeException.class);
}
     @Test
     void checkedException() {
         assertThatThrownBy(() -> service.checkedException())
                 .isInstanceOf(MyException.class);
}
     @Test
     void rollbackFor() {
         assertThatThrownBy(() -> service.rollbackFor())
                 .isInstanceOf(MyException.class);
     }
     @TestConfiguration
       static class RollbackTestConfig {
         @Bean
         RollbackService rollbackService() {
             return new RollbackService();
} }
     @Slf4j
     static class RollbackService {
//런타임 예외 발생: 롤백 @Transactional
public void runtimeException() {
             log.info("call runtimeException");
             throw new RuntimeException();
         }
//체크 예외 발생: 커밋
@Transactional
public void checkedException() throws MyException {
             log.info("call checkedException");
             throw new MyException();
         }
//체크 예외 rollbackFor 지정: 롤백 @Transactional(rollbackFor = MyException.class) public void rollbackFor() throws MyException {
             log.info("call rollbackFor");
             throw new MyException();
         }
}
     static class MyException extends Exception {
}
~~~

**runtimeException() 실행 - 런타임 예외**
~~~java
//런타임 예외 발생: 롤백 @Transactional
public void runtimeException() {
     log.info("call runtimeException");
     throw new RuntimeException();
 }
~~~

**checkedException() 실행 - 체크 예외**
~~~java
//체크 예외 발생: 커밋
@Transactional
public void checkedException() throws MyException {
     log.info("call checkedException");
     throw new MyException();
 }
~~~
**rollbackFor() 실행 - 체크 예외를 강제로 롤백**

@Transactional(rollbackFor = Exception.class)

~~~java
//체크 예외 rollbackFor 지정: 롤백 
@Transactional(rollbackFor = MyException.class) public void rollbackFor() throws MyException {
     log.info("call rollbackFor");
     throw new MyException();
 }
 ~~~

## 예외와 트랜잭션 커밋, 롤백


* 체크 예외: 비즈니스 의미가 있을 때 사용
* 언체크 예외: 복구 불가능한 예외

**정상**: 주문시 결제를 성공하면 주문 데이터를 저장하고 결제 상태를 `완료` 로 처리한다.

**시스템 예외**: 주문시 내부에 복구 불가능한 예외가 발생하면 전체 데이터를 롤백한다.

**비즈니스 예외**: 주문시 결제 잔고가 부족하면 주문 데이터를 저장하고, 결제 상태를 `대기` 로 처리한다.
이 경우 **고객에게 잔고 부족을 알리고 별도의 계좌로 입금하도록 안내한다.**

~~~java
@Transactional
public void order(Order order) throws NotEnoughMoneyException {
log.info("order 호출"); orderRepository.save(order);
log.info("결제 프로세스 진입");
if (order.getUsername().equals("예외")) {
log.info("시스템 예외 발생");
throw new RuntimeException("시스템 예외");
} else if (order.getUsername().equals("잔고부족")) { log.info("잔고 부족 비즈니스 예외 발생"); order.setPayStatus("대기");
throw new NotEnoughMoneyException("잔고가 부족합니다");
} else { //정상 승인
log.info("정상 승인");
order.setPayStatus("완료"); }
log.info("결제 프로세스 완료"); }
~~~

~~~java
package hello.springtx.order;
 public class NotEnoughMoneyException extends Exception {
     public NotEnoughMoneyException(String message) {
         super(message);
} }
     @Test
     void bizException() {
//given
Order order = new Order(); order.setUsername("잔고부족");
//when
try {
orderService.order(order); fail("잔고 부족 예외가 발생해야 합니다.");
} catch (NotEnoughMoneyException e) {
log.info("고객에게 잔고 부족을 알리고 별도의 계좌로 입금하도록 안내");
}
     }
~~~

**정리**
* `NotEnoughMoneyException` 은 시스템에 문제가 발생한 것이 아니라, 비즈니스 문제 상황을 예외를 통해 알
려준다. 마치 예외가 리턴 값 처럼 사용된다. 따라서 이 경우에는 트랜잭션을 커밋하는 것이 맞다. 이 경우 롤백하 면 생성한 `Order` 자체가 사라진다. 그러면 고객에게 잔고 부족을 알리고 별도의 계좌로 입금하도록 안내해도 주 문( `Order` ) 자체가 사라지기 때문에 문제가 된다.

* 그런데 비즈니스 상황에 따라 체크 예외의 경우에도 트랜잭션을 커밋하지 않고, 롤백하고 싶을 수 있다. 이때는 `rollbackFor` 옵션을 사용하면 된다.

* 런타임 예외는 항상 롤백된다. 체크 예외의 경우 `rollbackFor` 옵션을 사용해서 비즈니스 상황에 따라서 커밋 과 롤백을 선택하면 된다