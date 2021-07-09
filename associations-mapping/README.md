# 연관관계 매핑

## 객체 연관관계 vs 테이블 연관관계
1. 객체는 참조(주소)로 연관관계를 맺는다.
- 두 개의 객체 A와 B가 있을 때, A.getB()를 통해 다른 객체를 참조한다. 객체 연관관계는 단방향이다.

2. 테이블은 외래 키로 연관관계를 맺는다.
- A JOIN B, B JOIN A로 테이블은 연관관계가 양방향이다.

## @JoinColumn
외래 키를 매핑할 때 사용하는 어노테이션이다.

- name: 매핑할 외래 키 이름
- referencedColumnName: 외래 키가 참조하는 대상 테이블의 컬럼명

```
@Repeatable(JoinColumns.class)
@Target({METHOD, FIELD})
@Retention(RUNTIME)
public @interface JoinColumn {

    String name() default "";

    String referencedColumnName() default "";

    boolean unique() default false;

    boolean nullable() default true;

    boolean insertable() default true;

    boolean updatable() default true;

    String columnDefinition() default "";

    String table() default "";

    ForeignKey foreignKey() default @ForeignKey(PROVIDER_DEFAULT);
}
```

## @ManyToOne
다대일 관계에서 사용하는 어노테이션이다.

- fetch: 글로벌 패치 전략이며, 기본값은 EAGER이다.

```
@Target({METHOD, FIELD}) 
@Retention(RUNTIME)

public @interface ManyToOne {

    Class targetEntity() default void.class;

    CascadeType[] cascade() default {};

    FetchType fetch() default EAGER;

    boolean optional() default true;
}
```

## 양방향 연관관계 주의점
- 연관관계 주인이 외래 키를 관리한다. 외래 키를 갖고 있는 테이블과 매칭되는 엔티티가 연관관계 주인이다.
- 연관관계 주인이 아닌 곳에 값을 입력해도 DB에 반영될 때 무시된다.