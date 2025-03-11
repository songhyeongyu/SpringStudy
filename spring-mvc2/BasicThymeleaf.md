# Thymeleaf

**특징**

* 서버 사이드 html 랜더링
* 네츄럴 템플릿 : 순수 HTML을 그대로 유지하면서 뷰 템플릿도 사용할 수 있는 타임리프의 특징을 네츄럴 템플릿
* 스프링 통합지원

~~~
• 간단한 표현:
◦ 변수 표현식: ${...}
◦ 선택 변수 표현식: *{...} ◦ 메시지 표현식: #{...}
◦ 링크 URL 표현식: @{...} ◦ 조각 표현식: ~{...}
• 리터럴
◦ 텍스트: 'one text', 'Another one!',... ◦ 숫자: 0, 34, 3.0, 12.3,...
◦ 불린: true, false
◦ 널: null
◦ 리터럴 토큰: one, sometext, main,...
• 문자연산:
◦ 문자 합치기: +
◦ 리터럴 대체: |The name is ${name}| • 산술연산:
◦ Binary operators: +, -, *, /, %
◦ Minus sign (unary operator): - • 불린연산:
◦ Binary operators: and, or
◦ Boolean negation (unary operator): !, not • 비교와 동등:
◦ 비교:>,<,>=,<=(gt,lt,ge,le)
◦ 동등 연산: ==, != (eq, ne) • 조건연산:
◦ If-then: (if) ? (then)
◦ If-then-else: (if) ? (then) : (else) ◦ Default: (value) ?: (defaultvalue)
• 특별한 토큰:
◦ No-Operation: _
~~~


