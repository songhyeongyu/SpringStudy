# FileUpload

- 이름, 나이 , 첨부파일 이 세가지를 다 전송하려면 이름과 나이는 문자로 전송
첨부 파일은 바이너리로 전송 -> 문자와 바이너리를 동시에 전송하는 상황이 문제가 생긴다.

이러한 문제를 해결하기위해 HTTP는 `multipart/form-data` 을 제공한다.

![alt text](image-8.png)

이 방식을 사용하려면 Form 태그에 별도의 enctype=multipart/form-data를 지정해야된다.

`multipart/form-data` 방식은 다른 종류의 여러 파일과 폼의 내용 함께 전송할 수 있다. 

폼의 일반 데이터는 각 항목별로 문자가 전송되고, 파일의 경우 파일 이름과 Content-Type이 추가되고 바이너리 데이터가 전송된다.

## servlet과 파일 업로드 1

~~~java
  @GetMapping("/upload")
    public String newFile() {
        return "upload-form";
}
    @PostMapping("/upload")
    public String saveFileV1(HttpServletRequest request) throws
ServletException, IOException {
        log.info("request={}", request);
        String itemName = request.getParameter("itemName");
        log.info("itemName={}", itemName);
        Collection<Part> parts = request.getParts();
        log.info("parts={}", parts);
        return "upload-form";
}
~~~

~~~html
 <form th:action method="post" enctype="multipart/form-data">
         <ul>
<li>상품명 <input type="text" name="itemName"></li>
<li>파일<input type="file" name="file" ></li> </ul>
         <input type="submit"/>
     </form>
~~~

멀티파트는 일반적인 폼 요청인 `application/x-www-form-urlencoded`보다 훨씬 복잡하다.

## 스프링을 이용한 파일 업로드

스프링은 MultipartFile이라는 인터페이스로 멀티파트 파일을 매우 편리하게 지원한다.

~~~java
 @Slf4j
 @Controller
 @RequestMapping("/spring")
 public class SpringUploadController {
     @Value("${file.dir}")
     private String fileDir;
     @GetMapping("/upload")
     public String newFile() {
         return "upload-form";
}
     @PostMapping("/upload")
     public String saveFile(@RequestParam String itemName,
                            @RequestParam MultipartFile file, HttpServletRequest
 request) throws IOException {
         log.info("request={}", request);
         log.info("itemName={}", itemName);
         log.info("multipartFile={}", file);
if (!file.isEmpty()) {
String fullPath = fileDir + file.getOriginalFilename(); log.info("파일 저장 fullPath={}", fullPath); file.transferTo(new File(fullPath));
}
         return "upload-form";
     }
~~~

`@RequestParam MultipartFile file` 업로드하는 html Form의 name에 마추어 적용하면 된다.

~~~java
@Controller
@RequiredArgsConstructor
public class ItemController {
    private final ItemRepository itemRepository;
    private final FileStore fileStore;
    @GetMapping("/items/new")
    public String newItem(@ModelAttribute ItemForm form) {
        return "item-form";
    }
    @PostMapping("/items/new")
    public String saveItem(@ModelAttribute ItemForm form, RedirectAttributes
redirectAttributes) throws IOException {
        UploadFile attachFile = fileStore.storeFile(form.getAttachFile());
        List<UploadFile> storeImageFiles =
fileStore.storeFiles(form.getImageFiles());
//데이터베이스에 저장
Item item = new Item(); item.setItemName(form.getItemName()); item.setAttachFile(attachFile); item.setImageFiles(storeImageFiles); itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", item.getId());
        return "redirect:/items/{itemId}";
}
    @GetMapping("/items/{id}")
    public String items(@PathVariable Long id, Model model) {
        Item item = itemRepository.findById(id);
        model.addAttribute("item", item);
                return "item-view";
    }
    @ResponseBody
    @GetMapping("/images/{filename}")
    public Resource downloadImage(@PathVariable String filename) throws
MalformedURLException {
        return new UrlResource("file:" + fileStore.getFullPath(filename));
}
    @GetMapping("/attach/{itemId}")
    public ResponseEntity<Resource> downloadAttach(@PathVariable Long itemId)
throws MalformedURLException {
        Item item = itemRepository.findById(itemId);
        String storeFileName = item.getAttachFile().getStoreFileName();
        String uploadFileName = item.getAttachFile().getUploadFileName();
        UrlResource resource = new UrlResource("file:" +
fileStore.getFullPath(storeFileName));
        log.info("uploadFileName={}", uploadFileName);
        String encodedUploadFileName = UriUtils.encode(uploadFileName,
StandardCharsets.UTF_8);
        String contentDisposition = "attachment; filename=\"" +
encodedUploadFileName + "\"";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .body(resource);
}
}
~~~

* `@GetMapping("/items/new")` : 등록 폼을 보여준다.
* `@PostMapping("/items/new")` : 폼의 데이터를 저장하고 보여주는 화면으로 리다이렉트 한다. 
* `@GetMapping("/items/{id}")` : 상품을 보여준다.
* `@GetMapping("/images/{filename}")` : `<img>` 태그로 이미지를 조회할 때 사용한다. `UrlResource` 로 이미지 파일을 읽어서 `@ResponseBody` 로 이미지 바이너리를 반환한다. 
* `@GetMapping("/attach/{itemId}")` : 파일을 다운로드 할 때 실행한다. 예제를 더 단순화 할 수 있지만,
파일 다운로드 시 권한 체크같은 복잡한 상황까지 가정한다 생각하고 이미지 `id` 를 요청하도록 했다. 파일 다운로 드시에는 고객이 업로드한 파일 이름으로 다운로드 하는게 좋다.