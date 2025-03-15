# Message

여러 화면에 보이는 단순한 상품명,가격 수량 등 label에 있는 단어를 변경하려면 다음 화면들을 찾아가서 변경해야 되는데 화면 수가 많으면 점점 문제가 된다.

**message.properties**
~~~
item = 상품
item.id = 상품 ID
imte.itemNAme = 상품명
item.price= 가격
item.quantity= 수량
~~~
key 값으로 불러서 사용

**addForm.html**
`<label for="itemName" th:text="#{item.itemName}"></label>`

**message_en.properties**
~~~
item=Item
item.id=Item ID
item.itemName=Item Name
item.price=price
item.quantity=quantity
~~~

이렇게 사이트를 국제화하면 된다.
한국에서 접근한 것인지 영어에서 접근한 것인지 인식하는 방법은 HTTP `accept-language`헤더 값을 사용하거나 사용자가 언어를 선택하도록 처리하면 된다.


## 스프링 메시지 소스 설정

**spring에서 직접 등록**

~~~java
@Bean
 public MessageSource messageSource() {
     ResourceBundleMessageSource messageSource = new
ResourceBundleMessageSource();
    messageSource.setBasenames("messages", "errors");
    messageSource.setDefaultEncoding("utf-8");
    return messageSource;
}
~~~

**spring boot 등록**

`application.properties`
~~~
`spring.messages.basename=messages
~~~



## 스프링 메시지 사용

~~~java
 public interface MessageSource {
     String getMessage(String code, @Nullable Object[] args, @Nullable String
 defaultMessage, Locale locale);
     String getMessage(String code, @Nullable Object[] args, Locale locale)
throws NoSuchMessageException;
 }
~~~
messageSource 인터페이스를 보면 코드를 포함한 일부 파라미터로 메시지 읽어오는 기능을 제공한다.


## 웹 애플리케이션에 메시지 적용

타임리프의 메시지 표현식 #{...}를 사용하면 스프링의 메시지를 편하게 조회할 수 있다.

**렌더링 전**

~~~
 <div th:text="#{label.item}"></h2>
~~~

**렌더링 후**

~~~
<div>상품</h2>
~~~

~~~html
<label for="itemName" th:text="#{label.item.itemName}">상품명</label>
<input type="text" id="itemName" th:field="*{itemName}" class="form- control" placeholder="이름을 입력하세요">
</div> <div>
<label for="price" th:text="#{label.item.price}">가격</label>
<input type="text" id="price" th:field="*{price}" class="form- control" placeholder="가격을 입력하세요">
</div> <div>
<label for="quantity" th:text="#{label.item.quantity}">수량</label>
<input type="text" id="quantity" th:field="*{quantity}" class="form- control" placeholder="수량을 입력하세요">
</div>
~~~

**파라미터의 사용**

~~~
`hello.name=안녕 {0}`

`<p th:text="#{hello.name(${item.itemName})}"></p>`
~~~
