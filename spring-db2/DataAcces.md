# 데이터 접근 기술

1. SQL Mapper
    * JdbcTemplate
    * MyBatis
2. ORM
    * JPA, Hibernate


**SQL Mapper**

* 개발자는 SQL만 작성하면 해당 SQL 결과를 객체로 편리하게 매핑
* JDBC를 직섭 사용해 중복 제거

**ORM**

*  SQL mapper는 SQL을 개발자가 직접 작성하지만, JPA를 사용하면 기본적인 SQL은 JPA가 대신 작성


### 프로젝트 구조

**도메인 분석**
~~~java
 @Data
 public class Item {
     private Long id;
     private String itemName;
     private Integer price;
     private Integer quantity;
     public Item() {
     }
     public Item(String itemName, Integer price, Integer quantity) {
         this.itemName = itemName;
         this.price = price;
         this.quantity = quantity;
} 
}
~~~

**Repoisitory**
~~~java

 public interface ItemRepository {
     Item save(Item item);
     void update(Long itemId, ItemUpdateDto updateParam);
     Optional<Item> findById(Long id);
     List<Item> findAll(ItemSearchCond cond);
}
~~~

* 메모리 구현체에서 다양한 접근 기술 구현체로 변경하기 위해 인터페이스 도입


**ItemSearchCond**
~~~java

 @Data
 public class ItemSearchCond {
     private String itemName;
     private Integer maxPrice;
     public ItemSearchCond() {
     }
     public ItemSearchCond(String itemName, Integer maxPrice) {
         this.itemName = itemName;
         this.maxPrice = maxPrice;
} }
~~~
* 검색 조건으로 사용

**ItemUpdateDto**

~~~java
@Data
 public class ItemUpdateDto {
     private String itemName;
     private Integer price;
     private Integer quantity;
     public ItemUpdateDto() {
     }
     public ItemUpdateDto(String itemName, Integer price, Integer quantity) {
         this.itemName = itemName;
         this.price = price;
         this.quantity = quantity;} 
}
~~~
* 상품을 수정할 때 사용하는 객체
* 데이터를 전달하는 용도로 DTO 붙임

**DTO(data transfer object)**

* 데이터 전송 객체
* DTO는 기능은 없고 데이터 전달만 하는 용도로 사용

~~~java
 @Override
     public Item save(Item item) {
         item.setId(++sequence);
         store.put(item.getId(), item);
         return item;
     }

@Override
public void update(Long itemId, ItemUpdateDto updateParam) {
    Item findItem = findById(itemId).orElseThrow();
    findItem.setItemName(updateParam.getItemName());
    findItem.setPrice(updateParam.getPrice());
    findItem.setQuantity(updateParam.getQuantity());
}
~~~
* ItemRepo 인터페이스를 구현한 메모리 저장소
* Optional을 반환해야되서 ofNullalbe사용

**ItemService 인터페이스**

~~~java
public interface ItemService {
     Item save(Item item);
     void update(Long itemId, ItemUpdateDto updateParam);
     Optional<Item> findById(Long id);
     List<Item> findItems(ItemSearchCond itemSearch);
 }
 ~~~

 **itemServiceV1**
 ~~~java
 @Service
@RequiredArgsConstructor
public class ItemServiceV1 implements ItemService {
    private final ItemRepository itemRepository;
    @Override
    public Item save(Item item) {
        return itemRepository.save(item);
    }
    @Override
    public void update(Long itemId, ItemUpdateDto updateParam) {
        itemRepository.update(itemId, updateParam);
    }
    @Override
    public Optional<Item> findById(Long id) {
        return itemRepository.findById(id);
    }
    @Override
    public List<Item> findItems(ItemSearchCond cond) {
        return itemRepository.findAll(cond);
    }
}
~~~
* 단순히 리포지토리에 위임

~~~java
@Controller
 @RequestMapping("/items")
 @RequiredArgsConstructor
 public class ItemController {
     private final ItemService itemService;
@GetMapping
     public String items(@ModelAttribute("itemSearch") ItemSearchCond itemSearch,
 Model model) {List<Item> items = itemService.findItems(itemSearch);
        model.addAttribute("items", items);
        return "items";
}
    @GetMapping("/{itemId}")
    public String item(@PathVariable long itemId, Model model) {
        Item item = itemService.findById(itemId).get();
        model.addAttribute("item", item);
        return "item";
}
    @GetMapping("/add")
    public String addForm() {
        return "addForm";
    }
    @PostMapping("/add")
    public String addItem(@ModelAttribute Item item, RedirectAttributes
redirectAttributes) {
        Item savedItem = itemService.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/items/{itemId}";
}
    @GetMapping("/{itemId}/edit")
    public String editForm(@PathVariable Long itemId, Model model) {
        Item item = itemService.findById(itemId).get();
        model.addAttribute("item", item);
        return "editForm";
}
    @PostMapping("/{itemId}/edit")
    public String edit(@PathVariable Long itemId, @ModelAttribute ItemUpdateDto
updateParam) {
        itemService.update(itemId, updateParam);
        return "redirect:/items/{itemId}";
    }
}
~~~
