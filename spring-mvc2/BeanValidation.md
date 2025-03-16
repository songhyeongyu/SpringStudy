# Bean Validation

~~~java
public class Item {
     private Long id;
     @NotBlank
     private String itemName;
     @NotNull
     @Range(min = 1000, max = 1000000)
     private Integer price;
     @NotNull
     @Max(9999)
     private Integer quantity;
}
~~~

**Bean Validation** 이란?

Bean Validation은 기술 표준이다.

## 시작
순수한 Bean Validation 사용법 부터 테스트 코드로 알아보자

**의존관계 추가**

`implementation 'org.springframework.boot:spring-boot-starter-validation'`

**검증 애노테이션**
`@NotBlank` : 빈값 + 공백만 있는 경우 허용x
`@NotNull` : null 허용 x
`@Range(min= xxx, max = xxx)` : 범위 안의 값
`@Max(9999)` : 최대 9999까지만 허용

**정리**
이렇게 빈 검증기를 직접 사용 -> 스프링은 이미 개발자를 위해 빈 검증기를 스프링에 완전히 통합

## V3

**Bean Validation** - 적용

~~~java

    @PostMapping("/add")
    public String addItem(@Validated @ModelAttribute Item item, BindingResult
bindingResult, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            log.info("errors={}", bindingResult);
            return "validation/v3/addForm";
}
//성공 로직
Item savedItem = itemRepository.save(item); redirectAttributes.addAttribute("itemId", savedItem.getId()); redirectAttributes.addAttribute("status", true);
return "redirect:/validation/v3/items/{itemId}";
}
~~~

**스프링 MVC는 어떻게 BeanValidator를 사용?**
`validation`라이버러리를 넣으면 자동으로 bean Validator를 인지하고 스프링에 통합한다.

**스프링 부트는 자동으로 글로벌 Validator로 등록한다.**

LocalValidatorFactoryBean을 글로벌 Validator로 등록된다. -> 검증 오류가 발생하면 filedError, ObjectError를 생성해서 BindResult에 담아준다.


**검증 순서**

1. @ModelAttribute 각각의 필드에 타입 변환 시도
    1. 성공하면 다음
    2. 실패하면 typeMismatch로 FieldError 추가
2. Validator 적용

**바인딩에 성공한 필드만 Bean Validation 사용**

BeanValidator는 바인딩에 실패한 필드는 BeanValidation을 적용하지 않는다.


@ModelAttribute -> 각각의 필드 타입 변화시도 -> 변환에 성공한 필드만 BeanValidation 적용

## Bean Validation - 에러 코드

**BenaValidation** 메시지 찾는 순서

1. 생성된 메시지 코드 순서대로 `messageSource`에 메시지 찾기
2. 에노테이션의 `message` 속성 사용 -> `@NotBlank(message = "공백! {0}")`
3. 라이브러리가 제공하는 기본 값 사용 -> 공백일 수 없습니다.

**애노테이션의 message**
~~~java
@NotBlank(message = "공백은 입력할 수 없습니다.") private String itemName;
~~~

@NotBlank 생성 방식
* NotBlank.item.itemName
* NotBlank.itemName 
* NotBlank.java.lang.String
* NotBlank


## Bean Validation - 오브젝트 오류

특정 필드가 아닌 해당 오브젝트 오류는 어떻게 처리할까?


~~~java
 @Data
 @ScriptAssert(lang = "javascript", script = "_this.price * _this.quantity >=
 10000")
 public class Item {
//...
}
~~~
하지만 이런식으로 사용하면 객체의 범위를 넘어서는 경우들도 있는데 그런 경우 대응이 어렵다

**글로벌 오류 추가**
~~~java
 @PostMapping("/add")
 public String addItem(@Validated @ModelAttribute Item item, BindingResult
 bindingResult, RedirectAttributes redirectAttributes) {
//특정 필드 예외가 아닌 전체 예외
if (item.getPrice() != null && item.getQuantity() != null) {
         int resultPrice = item.getPrice() * item.getQuantity();
         if (resultPrice < 10000) {
             bindingResult.reject("totalPriceMin", new Object[]{10000,
 resultPrice}, null);
} }
     if (bindingResult.hasErrors()) {
         log.info("errors={}", bindingResult);
         return "validation/v3/addForm";
}
//성공 로직
Item savedItem = itemRepository.save(item); redirectAttributes.addAttribute("itemId", savedItem.getId()); redirectAttributes.addAttribute("status", true);
return "redirect:/validation/v3/items/{itemId}";
}
~~~

## Bean Valdation - 한계

데이터를 등록할 때와 수정할 때는 요구사항이 다를 수 있다.

**수정시 요구사항**
* 등록시에는 `quantity` 수량을 최대 9999까지 등록 하지만 수정시에는 **무제한**으로 변경
* 등록시에는 *id*에 값이 없어도 되지만 수정시에는 **id 값이 필수**


### groups

**방법 2가지**
* groups 기능 활용
* item을 사용하지 않고, itemSaveForm, itemUpdateForm 같은 폼 전송을 위한 별도의 모델 객체를 만들어서 사용

**groups 기능 사용**

~~~java
 package hello.itemservice.domain.item;
 public interface SaveCheck {
}
 package hello.itemservice.domain.item;
 public interface UpdateCheck {
}

@NotNull(groups = UpdateCheck.class) //수정시에만 적용 private Long id;
     @NotBlank(groups = {SaveCheck.class, UpdateCheck.class})
     private String itemName;

@PostMapping("/add")
 public String addItemV2(@Validated(SaveCheck.class) @ModelAttribute Item item,
 BindingResult bindingResult, RedirectAttributes redirectAttributes) {
//...
 }
  @PostMapping("/{itemId}/edit")
 public String editV2(@PathVariable Long itemId, @Validated(UpdateCheck.class)
 @ModelAttribute Item item, BindingResult bindingResult) {
//...
}
~~~
* edit() -> UpdateCheck.class 적용


**정리**
groups 기능을 사용해서 등록과 수정시에 각각 다르게 검증을 할 수 있다. 하지만 복잡도가 올라갔다.


### Form 전송 객체 분리

**소개**
실무에서는 groups를 잘 사용하지 않는다. 등록시 폼에서 전달하는 데이터가 Item 도메인 객체와 딱 맞지 않기 때문이다.

**폼 데이터 전달에 item 도메인 객체 사용**
form -> Item -> Controller -> Item -> Repository
* 장점 : item 도메인 객체를 컨트롤러, 리포지토리 까지 직접 전달해서 간단하다.
* 단점 : 간단한 경우에만 적용 -> 수정시 검증이 중복 및 groups를 사용

**폼 데이터 전달을 위한 별도의 객체 사용**
* form -> ItemSaveForm -> Controller -> Item 생성 -> Repository

* 장점 : 전송하는 폼 데이터가 복잡해도 거기에 맞춘 별도의 폼 객체를 사용해서 데이터를 전달 받을 수 있다.
검증이 중복 안됨
* 단점 : 폼 데이터를 기반으로 컨트롤러에서 item 객체를 생성하는 변환 과정이 추가

~~~java
 @Data
 public class ItemSaveForm {
     @NotBlank
     private String itemName;
     @NotNull
     @Range(min = 1000, max = 1000000)
     private Integer price;
     @NotNull
     @Max(value = 9999)
     private Integer quantity;
}
 @Data
 public class ItemUpdateForm {
     @NotNull
     private Long id;
     @NotBlank
     private String itemName;
     @NotNull
     @Range(min = 1000, max = 1000000)
     private Integer price;
//수정에서는 수량은 자유롭게 변경할 수 있다. 
private Integer quantity;
 }
~~~

~~~java
 @PostMapping("/add")
    public String addItem(@Validated @ModelAttribute("item") ItemSaveForm form,
BindingResult bindingResult, RedirectAttributes redirectAttributes) {
//특정 필드 예외가 아닌 전체 예외
if (form.getPrice() != null && form.getQuantity() != null) {
            int resultPrice = form.getPrice() * form.getQuantity();
            if (resultPrice < 10000) {
                bindingResult.reject("totalPriceMin", new Object[]{10000,
resultPrice}, null);
} }
        if (bindingResult.hasErrors()) {
            log.info("errors={}", bindingResult);
            return "validation/v4/addForm";
}
//성공 로직
Item item = new Item(); item.setItemName(form.getItemName()); item.setPrice(form.getPrice()); item.setQuantity(form.getQuantity());
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v4/items/{itemId}";
}
  @PostMapping("/{itemId}/edit")
    public String edit(@PathVariable Long itemId, @Validated
@ModelAttribute("item") ItemUpdateForm form, BindingResult bindingResult) {
//특정 필드 예외가 아닌 전체 예외
if (form.getPrice() != null && form.getQuantity() != null) {
            int resultPrice = form.getPrice() * form.getQuantity();
            if (resultPrice < 10000) {
                bindingResult.reject("totalPriceMin", new Object[]{10000,
resultPrice}, null);
} }
        if (bindingResult.hasErrors()) {
            log.info("errors={}", bindingResult);
            return "validation/v4/editForm";
}
        Item itemParam = new Item();
        itemParam.setItemName(form.getItemName());
        itemParam.setPrice(form.getPrice());
        itemParam.setQuantity(form.getQuantity());
        itemRepository.update(itemId, itemParam);
        return "redirect:/validation/v4/items/{itemId}";
    }
~~~

* @ModelAttribute("item") item 대신에 itemSaveForm을 전달받는다. @Validated로 검증도 수행하고 BindingResult로 검증 결과도 받는다.

**주의**
@ModelAttribute("item")에 item이름을 넣어준 부분 주의 -> itemSaveForm이라는 이름으로 MVC Model에 담기게 된다.

## Bean Validation -HTTP 메시지 컨버터

**ValidationTempApicontroller 생성**
~~~java
@Slf4j
 @RestController
 @RequestMapping("/validation/api/items")
 public class ValidationItemApiController {
     @PostMapping("/add")
     public Object addItem(@RequestBody @Validated ItemSaveForm form,
BindingResult bindingResult) { log.info("API 컨트롤러 호출");
if (bindingResult.hasErrors()) {
log.info("검증 오류 발생 errors={}", bindingResult); return bindingResult.getAllErrors();
}
log.info("성공 로직 실행");
         return form;
     }
}
~~~

@ModelAttrivute vs @RequestBody

Http 요청 파라미터를 처리하는 @ModelAttrivbute는 각각의 필드 단위로 세밀하게 적용된다. 그래서 특정 필드에 타입이 맞이 않는 오류가 발생해도 나머지 필드는 정상

@RequestBody는 전체 객체 단위로 적용된다.
따라서 메시지 컨버터의 작동이 성공해서 ItemSaveForm객체를 만들어야 valid, validated가 적용

* ModelAttribute는 필드 단위로 정교하게 바인딩
특정 필드가 바인딩 되지 않아도 나머지 필드는 정상 바인딩
* @Request는 MessageConvert 단계에서 JSON 데이터를 객체로 변경하지 못하면 이후 단계가 진행 x
