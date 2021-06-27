# 엔티티 매핑 

## @Entity
테이블과 매핑하는 클래스에 `@Entity` 어노테이션을 필수로 붙여야 한다. `@Entity` 어노테이션이 붙은 클래스는 JPA가 관리하게 된다.

속성으로 name이 있으며, 엔티티 이름을 지정할 수 있다. 따로 지정 하지 않으면 기본 값으로 클래스 이름을 사용한다.

```
@Documented
@Target(TYPE)
@Retention(RUNTIME)
public @interface Entity {

	String name() default "";
}
```

`@Entity` 적용 시 주의사항

- 기본 생성자는 필수 (public or protected 생성자)
- final 클래스, enum, interface, inner 클래스에서 사용할 수 없다
- 저장할 필드에 final를 사용 하면 안된다.

Java와 다르게 Kotlin에서 Entity를 사용할 때, 아래 플러그인과 설정이 추가적으로 필요하다. Kotlin 클래스는 기본적으로 final 클래스이며, 주생성자를 사용하게 되면 컴파일러가 기본 생성자를 생성해주지 않기 때문에 플러그인 도움을 받아서 해결할 수 있다.
 
```
plugins {
    kotlin("plugin.allopen") version "1.4.21"
    kotlin("plugin.noarg") version "1.4.21"
}

allOpen {
    annotation("javax.persistence.Entity")
}

noArg {
    annotation("javax.persistence.Entity")
}
```

## @Table
엔티티와 매핑할 테이블을 지정한다. 테이블을 직접 지정하지 않으면 엔티티 이름을 사용하게 된다.

```
@Target(TYPE) 
@Retention(RUNTIME)
public @interface Table {

    /**
     * (Optional) The name of the table.
     */
    String name() default "";

    /** (Optional) The catalog of the table.
     */
    String catalog() default "";

    /** (Optional) The schema of the table.
     */
    String schema() default "";

    /**
     * (Optional) Unique constraints that are to be placed on 
     * the table.
     */
    UniqueConstraint[] uniqueConstraints() default {};

    /**
     * (Optional) Indexes for the table.
     */
    Index[] indexes() default {};
}
```

### 스키마 자동 생성
persistence.xml에 `hibernate.hbm2ddl.auto` 프로퍼티를 정의해서 애플리케이션 실행 시점에 데이터베이스 테이블을 자동으로 생성, 수정, 검증 등을 할 수 있다. 옵션 값으로는 총 7개가 있다.

- CREATE : 기존 테이블 정보를 삭제하고 다시 엔티티 정보를 기반으로 생성 
- CREATE_DROP : 애플리케이션 종료할 때 생성한 DDL를 제거 
- CREATE_ONLY
- DROP
- NONE: 아무것도 하지 않는 옵션 (기본값)
- UPDATE : 데이터베이스 스키마와 엔티티 정보를 비교해서 update(alter)를 수행
- VALIDATE : 데이터베이스 스키마와 엔티티 정보를 비교해서 차이가 있는지 확인

## DDL 생성 기능
스키마 자동 생성 기능에 제약조건을 주고 싶을 때는 `@Column`, `@UniqueConstraint`, `@Index` 등 어노테이션을 Entity 클래스에 명시하면 된다.

클래스 프로퍼티에 `@Column(nullable=false, legnth=10)` 어노테이션을 추가하면, 해당 컬럼은 not null 제약조건과 길이 값 10인 컬럼이 생성된다.

클래스 레벨에 `@UniqueConstraint`, `@Index` 어노테이션을 붙임으로써 자동으로 생성되는 DDL에 제약 조건과 인덱스 정보를 추가할 수 있다.

## 기본 키 매핑
`@Id` 어노테이션을 사용해서 기본 키를 할당 할 수 있다. 데이터베이스마다 기본 키를 생성하는 방식이 다르기 때문에 JPA는 기본 키 생성 전략 옵션을 제공한다.

- 직접 할당 : 애플리케이션에서 기본 키를 직접 할당
- 자동 생성 : 대리 키 사용 방식
    - IDENTITY : 기본 키 생성을 데이터베이스에 위임
    - SEQUENCE : 데이터베이스 시퀀스를 사용해서 기본 키를 할당
    - TABLE: 키 생성 테이블 사용
    
```
@Entity
@Table(name = "Member")
class Member(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long
}
```

### 기본 키 매핑 정리
영속성 컨텍스트는 엔티티를 식별자 값으로 구분한다. 따라서 엔티티를 영속 상태로 만들기 위해서는 식별자 값이 반드시 있어야 한다.

- 직접 할당 : em.persist() 호출하기 전에 애플리케이션에서 직접 식별자 값을 할당한다.
- SEQUENCE : SEQUENCE 오브젝트에서 식별자 값을 얻고 나서 영속성 컨텍스트에 저장한다.
- TABLE : SEQUENCE 용 테이블에서 식별자 값을 얻고 나서 영속성 컨텍스트에 저장한다.
- IDENTITY : 데이터베이스에 엔티티를 저장해서 식별자 값을 얻은 후 영속성 컨텍스트에 저장한다.

## 필드와 컬럼 매핑

### @Column
`@Column` 어노테이션은 객체 필드를 테이블 컬럼에 매핑할 때 사용한다. 어노테이션에 여러 속성이 있지만 주로 사용되는 것은 name, nullable, length 이다.

엔티티 클래스에서 `@Column` 어노테이션이 필드에 붙어 있지 않아도 기본 값으로 설정된다.

```
@Target({METHOD, FIELD}) 
@Retention(RUNTIME)
public @interface Column {

    String name() default "";
    
    boolean unique() default false;

    boolean nullable() default true;

    boolean insertable() default true;

    boolean updatable() default true;

    String columnDefinition() default "";

    String table() default "";

    int length() default 255;

    int precision() default 0;

    int scale() default 0;
}
```

### @Enumerated
enum 타입을 매핑할 때 사용한다. enum 순서는 바뀔 수 있으므로 name을 저장하도록 value 속성에 EnumType.STRING를 지정한다. 

기본값은 enum 순서를 저장하는 EnumType.ORDINAL 이므로 주의해야 한다. 

```
@Entity
@Table(name = "Member")
class Member(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long,

    val username: String,

    var age: Int,

    @Enumerated(EnumType.STRING)
    val roleType: RoleType
)
```

#### EnumType

```
@Target({METHOD, FIELD}) 
@Retention(RUNTIME)
public @interface Enumerated {

    EnumType value() default ORDINAL;
}
```

### @LOB
매핑하는 타입이 문자면 CLOB으로 매핑하고, 나머지는 BLOB으로 매핑한다.

### @Transient
필드를 테이블 컬럼과 매핑하지 않을 때 사용한다.

### @Access
JPA가 엔티티 데이터에 접근하는 방식을 지정한다. `@Access` 어노테이션을 설정하지 않으면, `@Id` 위치를 기준으로 접근 방식이 결정된다.

- 필드 접근 : AccessType.FIELD로 지정한다. 필드에 직접 접근하며 권한이 private 이어도 가능하다.
- 프로퍼티 접근 : AccessType.PROPERTY로 지정한다. 게터를 사용한다.

#### 필드 기반 접근
`@Id` 어노테이션이 필드 위에 붙어 있기 때문에 필드 접근 방식을 사용한다.

```
@Entity(name = "Book")
public static class Book {

	@Id
	private Long id;

	private String title;

	private String author;

	//Getters and setters are omitted for brevity
}
```

#### 프로퍼티 기반 접근
`@Id` 어노테이션이 게터 위에 붙어 있기 때문에 프로퍼티 접근 방식을 사용한다.

```
@Entity(name = "Book")
public static class Book {

	private Long id;

	private String title;

	private String author;

	@Id
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}
}
```
