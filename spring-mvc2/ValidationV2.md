# Validatotr separation

**목표**

* 복잡한 검증 로직을 별도로 분리

**ItemValidator**

~~~java
@Component
public class ItemValidator implements Validator {
    @Override
    public boolean supports(Class<?> clazz) {
        return Item.class.isAssignableFrom(clazz);
    }
    @Override
    public void validate(Object target, Errors errors) {
        Item item = (Item) target;
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "itemName",
"required");
        if (item.getPrice() == null || item.getPrice() < 1000 || item.getPrice()
> 1000000) {
            errors.rejectValue("price", "range", new Object[]{1000, 1000000},
null);
}
        if (item.getQuantity() == null || item.getQuantity() > 10000) {
            errors.rejectValue("quantity", "max", new Object[]{9999}, null);
}
//특정 필드 예외가 아닌 전체 예외
if (item.getPrice() != null && item.getQuantity() != null) {
null);
}
} }
```
int resultPrice = item.getPrice() * item.getQuantity();
if (resultPrice < 10000) {
    errors.reject("totalPriceMin", new Object[]{10000, resultPrice},
}
~~~

스프링은 검증을 체계적으로 제공하깅 위해 다음 인터페이스를 제공
~~~java
 public interface Validator {
     boolean supports(Class<?> clazz);
     void validate(Object target, Errors errors);
}
~~~
* `supports() {}` : 해당 검증기를 지원하는 여부 확인(뒤에서 설명)
* `validate(Object target, Errors errors)` : 검증 대상 객체와 `BindingResult

**ItemValidator 직접 호출**

~~~java

 private final ItemValidator itemValidator;
 @PostMapping("/add")
 public String addItemV5(@ModelAttribute Item item, BindingResult bindingResult,
 RedirectAttributes redirectAttributes) {
     itemValidator.validate(item, bindingResult);
     if (bindingResult.hasErrors()) {
         log.info("errors={}", bindingResult);
         return "validation/v2/addForm";
}
//성공 로직
Item savedItem = itemRepository.save(item); redirectAttributes.addAttribute("itemId", savedItem.getId()); redirectAttributes.addAttribute("status", true);
return "redirect:/validation/v2/items/{itemId}";
}
~~~

# Validator separtion2

~~~java
@InitBinder
 public void init(WebDataBinder dataBinder) {
     log.info("init binder {}", dataBinder);
     dataBinder.addValidators(itemValidator);
 }
  @PostMapping("/add")
 public String addItemV6(@Validated @ModelAttribute Item item, BindingResult
 bindingResult, RedirectAttributes redirectAttributes) {
     if (bindingResult.hasErrors()) {
         log.info("errors={}", bindingResult);
         return "validation/v2/addForm";
}
//성공 로직
Item savedItem = itemRepository.save(item); redirectAttributes.addAttribute("itemId", savedItem.getId()); redirectAttributes.addAttribute("status", true);
return "redirect:/validation/v2/items/{itemId}";
}
~~~

**동작 방식**
@Validated는 검증기를 실행하라는 에노테이션 이 에노테이션이 붙으면 WebDataBinder에 등록한 검증기를 찾아서 실행

~~~java
 @Component
 public class ItemValidator implements Validator {
     @Override
     public boolean supports(Class<?> clazz) {
         return Item.class.isAssignableFrom(clazz);
     }
@Override
     public void validate(Object target, Errors errors) {...}
 }
~~~
