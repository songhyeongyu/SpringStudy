# Bean lifeCycle

데이터베이스 커넥션, 네트워크 소켓처럼 애플리케이션 시작 및 종료 진행하려면 객체의 초기화와 종료 작업이 필요하다.

~~~java
  private String url;
public NetworkClient() {
System.out.println("생성자 호출, url = " + url); connect();
call("초기화 연결 메시지");
}
     public void setUrl(String url) {
         this.url = url;
}
//서비스 시작시 호출
public void connect() {
System.out.println("connect: " + url);
}
public void call(String message) {
         System.out.println("call: " + url + " message = " + message);
}
//서비스 종료시 호출
public void disconnect() {
         System.out.println("close: " + url);
     }

 @Test
     public void lifeCycleTest() {
         ConfigurableApplicationContext ac = new
 AnnotationConfigApplicationContext(LifeCycleConfig.class);
NetworkClient client = ac.getBean(NetworkClient.class); ac.close(); //스프링 컨테이너를 종료, ConfigurableApplicationContext 필요
     }
     @Configuration
     static class LifeCycleConfig {
         @Bean
         public NetworkClient networkClient() {
             NetworkClient networkClient = new NetworkClient();
             networkClient.setUrl("http://hello-spring.dev");
             return networkClient;
}
}
~~~
생성자 부분을 보면 url 정보없이 connect가 호출된다.

**당연히** 객체를 생성하는 단계에는 url이 없고, 객체를 생성한 다음에 외부에서 수정자 주입을 통해 `setUrl()`이 호출되야 된다.

스프링 빈은 간단하게 다음과 같은 라이프사이클을 가진다.

 **객체 생성** -> **의존관계 주입**

 스프링은 의존관계 주입이 완료되면 스프링 빈에게 콜백 메서드를 통해서 초기화 시점을 알려주는 다양한 기능 제공 -> 컨테이너가 종료되기 직전에 소멸 콜백!!


 **스프링 빈의 이벤트 라이프사이클**
**스프링 컨테이너 생성** -> **스프링 빈 생성** -> **의존관계 주입** -> **초기화 콜백** -> **사용** -> **소멸전 콜백**
-> **스프링 종료**

* **초기화 콜백**: 빈이 생성되고, 빈의 의존관계 주입이 완료된 후 호출 
* **소멸전 콜백**: 빈이 소멸되기 직전에 호출
스프링은 다양한 방식으로 생명주기 콜백을 지원한다.


3가지 빈 생명주기 콜잭지원
1. 인터페이스
2. 설정 정보에 초기화 메서드, 종료
3. @PostConstruct, @PreDestroy

## 인터페이스

~~~java
public class NetworkClient implements InitializingBean, DisposableBean {
     private String url;
public NetworkClient() {
System.out.println("생성자 호출, url = " + url);
}
 @Override
    public void afterPropertiesSet() throws Exception {
connect();
call("초기화 연결 메시지"); }
    @Override
    public void destroy() throws Exception {
        disConnect();
    }
}
~~~

1. initializingBean -> 초기화 지원
2. DisposableBean -> destroy 지원

**단점**
1. 스프링 전용 인터페이스에 의존한다.
2. 초기화, 소멸 메서드의 이름 변경 x
3. 외부 라이브러리 적용 x


## 빈 등록 초기화, 소멸 메서드 지정

~~~java
@Configuration
 static class LifeCycleConfig {
     @Bean(initMethod = "init", destroyMethod = "close")
     public NetworkClient networkClient() {
         NetworkClient networkClient = new NetworkClient();
         networkClient.setUrl("http://hello-spring.dev");
         return networkClient;
} }
~~~

**특징**

1. 메서드 이름을 자유롭게 설정 가능
2. 빈이 코드에 의존하지 않는다.
3. 외부 라이브러리에도 초기화, 종료 메서드를 적용 가능

## @PostConstruct, @PreDestroy

~~~java

     @PostConstruct
     public void init() {
System.out.println("NetworkClient.init"); connect();
call("초기화 연결 메시지");
}
     @PreDestroy
     public void close() {
         System.out.println("NetworkClient.close");
         disConnect();
     }
~~~

**특징**

1. 스프링에서 가장 권장
2. 유일한 단점은 외부 라이브러리에 적용 x



**정리**
* **@PostConstruct, @PreDestroy 애노테이션을 사용하자**
* 코드를 고칠 수 없는 외부 라이브러리를 초기화, 종료해야 하면 `@Bean` 의 `initMethod` , `destroyMethod` 를 사용하자.