# Aspect Oriented Programming

Aop가 왜 필요할까?

* cross curring concer vs core concern

공통 관심 사항을 조회하고 싶다

ex) 회원 가입 시간 + 회원 조회 시간


~~~java
public Long join(Member member) {
         long start = System.currentTimeMillis();
try {
validateDuplicateMember(member); //중복 회원 검증 memberRepository.save(member);
return member.getId();
         } finally {
             long finish = System.currentTimeMillis();
             long timeMs = finish - start;
             System.out.println("join " + timeMs + "ms");
} }
   public List<Member> findMembers() {
         long start = System.currentTimeMillis();
         try {
             return memberRepository.findAll();
         } finally {
             long finish = System.currentTimeMillis();
             long timeMs = finish - start;
             System.out.println("findMembers " + timeMs + "ms");
} }
~~~
이런 식으로 모든 method에 시간을 적용해서 올려줘야된다. -> 1000개의 method가 있으면 모든 method들에 대해 try문을 사용해서 구현해야 된다.

**문제**
* 회원가입, 회원 조회에 시간을 측정하는 기능은 핵심 관심 사항이 아니다. 
* 시간을 측정하는 로직은 공통 관심 사항이다.
* 시간을 측정하는 로직과 핵심 비즈니스의 로직이 섞여서 유지보수가 어렵다. 
* 시간을 측정하는 로직을 별도의 공통 로직으로 만들기 매우 어렵다.

**AOP를 활용한 해결**

~~~java
 @Component
 @Aspect
 public class TimeTraceAop { @Around("execution(* hello.hellospring..*(..))")
     public Object execute(ProceedingJoinPoint joinPoint) throws Throwable {
         long start = System.currentTimeMillis();
         System.out.println("START: " + joinPoint.toString());
         try {
             return joinPoint.proceed();
         } finally {
             long finish = System.currentTimeMillis();
             long timeMs = finish - start;
             System.out.println("END: " + joinPoint.toString()+ " " + timeMs + "ms")
         }
     }
     }
~~~

**해결**

* 회원가입, 회원 조회등 핵심 관심사항과 시간을 측정하는 공통 관심 사항을 분리한다. 
* 시간을 측정하는 로직을 별도의 공통 로직으로 만들었다.
* 핵심 관심 사항을 깔끔하게 유지할 수 있다.
* 변경이 필요하면 이 로직만 변경하면 된다.
* 원하는 적용 대상을 선택할 수 있다.