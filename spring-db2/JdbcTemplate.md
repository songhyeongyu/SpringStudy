# JdbcTemplate

**JdbcTemplate**

**장점**
* 설정의 편리함
    * Jdbc는 spring-jdbc 라이브러리에 포함되어 있는데, 이 라이버러리는 스프링 기본 라이브러리

    * 반복 문제 해결
        * 콜백 패턴을 사용해서, JDBC를 직접 사용할 때 발생하는 대부분의 반복 작업을 대신 처리
**단점**
* 동적 SQL을 처리하기 어렵다

~~~java
@Repository
public class JdbcTemplateItemRepositoryV1 implements ItemRepository {
    private final JdbcTemplate template;
    public JdbcTemplateItemRepositoryV1(DataSource dataSource) {
        this.template = new JdbcTemplate(dataSource);
}
    @Override
    public Item save(Item item) {
        String sql = "insert into item (item_name, price, quantity) values
(?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        template.update(connection -> {
{"id"});
//자동 증가 키
PreparedStatement ps = connection.prepareStatement(sql, new String[]
ps.setString(1, item.getItemName());
ps.setInt(2, item.getPrice());
ps.setInt(3, item.getQuantity());
return ps;
}, keyHolder);
long key = keyHolder.getKey().longValue();
item.setId(key);
return item;
}
~~~

**기본**
* ItempReposiory 인터페이스를 구현
* this.template = new JdbcTemplate(datasource)로 생성한다.

**save**

* template.update() : 데이터를 변경할 때 update를 사용
    * Insert,UPdate, delete sql에 사용
    * .update의 반환 값은 영향 받은 로우 수를 바노한
* 데이터를 저장할 때 pk 생성에 identity 방식을 사용 -> Pk인 ID값을 개발자가 지정하는 것이 아니라 비워두고 저장
* db에 Insert가 완료되어야지 pk값을 확인할 수 있다.

**update()**
* `template.update()` : 데이터를 변경할 때는 `update()` 를 사용하면 된다.
    * `?` 에 바인딩할 파라미터를 순서대로 전달하면 된다.
    * 반환값은해당쿼리의영향을받은로우수이다.여기서는 `where id=?` 를지정했기때문에 영향받은로우수는 최대 1개이다.

**findById()**
데이터 하나를 조회
* `template.queryForObject()`
    * 결과가 로우 하나일 때 사용
* ItemRepository.findById()` 인터페이스는 결과가 없을 때 `Optional` 을 반환해야 한다. 따라서 결과가
없으면 예외를 잡아서 `Optional.empty` 를 대신 반환하면 된다.


**findAll()**
데이터를 리스트로 조회한다. 그리고 검색 조건으로 적절한 데이터를 찾는다.

* `template.query()`
    * 결과가 없으면 빈 컬렉션을 반환한다.

**itemRowMapper()**
데이터베이스의 조회 결과를 객체로 변환할 때 사용한다.
JdbcTemplate이 다음과 같은 루프를 돌려주고, 개발자는 `RowMapper` 를 구현해서 그 내부 코드만 채운다고 이해하면 된다.

## 동적쿼리

결과를 검색하는 `findAll()` 에서 어려운 부분은 사용자가 검색하는 값에 따라서 실행하는 SQL이 동적으로 달려져 야 한다는 점이다.

상황에 따른 SQL을 동적으로 생성해야된다. 다양한 상황을 고려해야 되기 때문에 사용하기가 어려울 수도 있다.

## JdbcTeplate

**순서대로 바인딩**
~~~java
 String sql = "update item set item_name=?, quantity=?, price=? where id=?";
 template.update(sql,
         itemName,
         price,
         quantity,
         itemId);
~~~
결과적으로 price와 quantity가 바뀌는 문제가 발생한다. 모호함을 제거해서 코드를 명확하게 만들어 보자

## 이름지정 파라미터 생성
~~~java
 String sql = "insert into item (item_name, price, quantity) " +
                     "values (:itemName, :price, :quantity)";

String sql = "update item " +
                "set item_name=:itemName, price=:price, quantity=:quantity " +
                "where id=:id";
  String sql = "select id, item_name, price, quantity from item where id
= :id";
~~~

**기본**
* RepositoryV2는 ItemRepository 인터페이스를 구현
    * this.template = new NameParameterJdbcTemplate(dataSource)
    * 내부에 dataSource를 구현


**sava**
~~~java
 insert into item (item_name, price, quantity) " +
              "values (:itemName, :price, :quantity)"
~~~

## 이름지정 파라미터 2
다음 코드를 보면 param을 전달하는 것을 확인 가능

`template.update(sql, param, keyHolder);`

이름 지정 바인딩에서 자주 사용하는 파라미터의 종류는 크게 3가지가 있다.
* `Map`
단순히 map 사용
~~~java
 Map<String, Object> param = Map.of("id", id);
 Item item = template.queryForObject(sql, param, itemRowMapper());
~~~
* `MapSqlParameterSource` 
Map과 유사하지만 SQL 타입을 지정할 수 있어 Sql에 좀 더 특화된 기능을 제공
~~~java
SqlParameterSource param = new MapSqlParameterSource() .addValue("itemName", updateParam.getItemName()) .addValue("price", updateParam.getPrice()) .addValue("quantity", updateParam.getQuantity()) .addValue("id", itemId); //이 부분이 별도로 필요하다.
template.update(sql, param);
~~~
* `BeanPropertySqlParameterSource`
자바빈 프로퍼티 규약을 통해 자동으로 파라미터 객체 생성
~~~java
 SqlParameterSource param = new BeanPropertySqlParameterSource(item);
 KeyHolder keyHolder = new GeneratedKeyHolder();
template.update(sql, param, keyHolder);
~~~


**JdbcTemplateItemRepositoryV1 - itemRowMapper()**
`BeanPropertyRowMapper 사용 x`


~~~java
private RowMapper<Item> itemRowMapper() {
     return (rs, rowNum) -> {
         Item item = new Item();
         item.setId(rs.getLong("id"));
         item.setItemName(rs.getString("item_name"));
         item.setPrice(rs.getInt("price"));
         item.setQuantity(rs.getInt("quantity"));
         return item;
}; }
~~~

**적용**

~~~java

private RowMapper<Item> itemRowMapper() {
return BeanPropertyRowMapper.newInstance(Item.class); //camel 변환 지원
}
~~~
`BeanPropertyRowMapper` 는 `ResultSet` 의 결과를 받아서 자바빈 규약에 맞추어 데이터를 변환한다.

## SimpleJdbcInser

JdbcTemplate은 INSERT SQL를 직접 작성하지 않아도 되도록 `SimpleJdbcInsert` 라는 편리한 기능을 제공한 다.

~~~java
private final NamedParameterJdbcTemplate template;
private final SimpleJdbcInsert jdbcInsert;
public JdbcTemplateItemRepositoryV3(DataSource dataSource) {
    this.template = new NamedParameterJdbcTemplate(dataSource);
    this.jdbcInsert = new SimpleJdbcInsert(dataSource)
}
~~~
* `withTableName` : 데이터를 저장할 테이블 명을 지정한다.
* `usingGeneratedKeyColumns` : `key` 를 생성하는 PK 컬럼 명을 지정한다.
* `usingColumns` : INSERT SQL에 사용할 컬럼을 지정한다. 특정 값만 저장하고 싶을 때 사용한다. 생략할 수
있다.