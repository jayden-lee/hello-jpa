# 영속성 관리

## EntityManagerFactory와 EntityManager

EntityManagerFactory는 EntityManager를 만드는 클래스이며, 데이터 소스 정보 기준으로 생성된다.
여러 스레드가 동시에 접근해도 안전하기 때문에 스레드 간에 공유해도 된다.

EntityManagerFactory 객체는 Persistence 클래스의 정적 메서드 createEntityManagerFactory를 통해 생성할 수 있다. 
hibernate 구현체를 사용하고 있으면, HibernatePersistenceProvider를 사용해서 EntityManagerFactory를 생성하게 된다.

```java
public class Persistence {

    public static EntityManagerFactory createEntityManagerFactory(String persistenceUnitName, Map properties) {
    
        EntityManagerFactory emf = null;
        PersistenceProviderResolver resolver = PersistenceProviderResolverHolder.getPersistenceProviderResolver();
    
        List<PersistenceProvider> providers = resolver.getPersistenceProviders();
    
        for (PersistenceProvider provider : providers) {
            emf = provider.createEntityManagerFactory(persistenceUnitName, properties);
            if (emf != null) {
                break;
            }
        }
        if (emf == null) {
            throw new PersistenceException("No Persistence provider for EntityManager named " + persistenceUnitName);
        }
        return emf;
    }
}
```

EntityManager는 스레드에 안전하지 않기 때문에 여러 스레드간에 공유하지 않는다. EntityManager는 DB 커넥션을 사용하게 되는데, 필요한 시점까지 미루다가 가져온다.
트랜잭션을 시작하게 되면 커넥션을 획득한다.

## 영속성 컨텍스트
영속성 컨텍스트(Persistence Context)는 엔티티를 영구 저장하는 환경이라는 뜻이다. 엔티티 매니저는 영속성 컨텍스트를 생성해서 사용하고, 영속성 컨텍스트는 엔티티 매니저를 통해 접근하게 된다.
여러 엔티티 매니저가 같은 영속성 컨텍스트를 사용할 수도 있다.

## Entity 생명주기
엔티티에는 4가지 상태가 존재한다.

- 비영속(new/transient) : 영속성 컨텍스트와 관계없는 상태
- 영속(managed) : 영속성 컨텍스트에 저장된 상태
- 준영속(detached) : 영속성 컨텍스트에 저장되었다가 분리된 상태
- 삭제(removed) : 영속성 컨텍스트에서 엔티티가 삭제된 상태

### 비영속
엔티티 객체를 순수하게 생성하고 저장하지 않은 상태이다. 이 엔티티 객체는 영속성 컨테스트와 DB와 관련이 없다.

```
val member = Member(
    id = memberId,
    username = "memberName",
    age = 10
)
```

### 영속
엔티티 매니저를 통해서 엔티티를 영속성 컨텍스트에 저장하는 경우 엔티티는 영속 상태라 한다.
find() 또는 JPQL를 사용해서 조회한 엔티티도 영속성 컨텍스트가 관리하는 영속 상태이다.

```
em.persist(member)
```

### 준영속
영속성 컨텍스트가 관리하던 영속 상태의 엔티티를 영속성 컨텍스트가 더이상 관리하지 않으면 준영속 상태가 된다.
detach()를 호출하면 특정 엔티티를 준영속 상태로 바꿀 수 있다.

```
em.detach(member)
```

엔티티 매니저를 닫거나 초기화 해도 영속성 컨테스트가 관리하던 엔티티는 준영속 상태가 된다.

```
// 닫기
em.close()

// 초기화
em.clear()
```

### 삭제
엔티티를 영속성 컨테스트와 DB에서 삭제한다.

```
em.remove(member)
```

## 영속성 컨텍스트 특징

- 엔티티를 식별자 값으로 구분한다. 영속 상태는 식별자 값이 반드시 있어야 한다.
- 관리하고 있는 엔티티를 DB에 반영 하는 시점은 트랜잭션이 커밋하는 순간이다. 이를 flush라고 한다.
- 영속성 컨텍스트가 엔티티를 관리함으로써 1차 캐시, 동일성 보장, 트랜잭션 지원하는 쓰기 지연, 변경 감지, 지연로딩 등 장점이 있다.

## 엔티티 조회
영속성 컨텍스트는 내부에 캐시를 가지는데 이를 1차 캐시라 한다. 쉽게 설명하면 내부에 Map이 있는 것이고 Key는 식별자이다.

엔티티 매니저로 데이터를 조회하면 먼저 1차 캐시에서 엔티티를 찾게 되고, 엔티티가 없으면 DB에서 조회한다.
DB에서 조회한 엔티티는 영속성 컨텍스트의 1차 캐시에 저장한 후 영속 상태의 엔티티로 반환한다.

```
em.find(Member::class.java, memberId)
``` 

### 영속 엔티티의 동일성 보장
동일한 식별자의 엔티티를 조회하면 동일한 인스턴스를 반환한다.

```
val findMember1 = em.find(Member::class.java, memberId)
val findMember2 = em.find(Member::class.java, memberId)

// true
print(findMember1 === findMember2) 
``` 

## 엔티티 등록
엔티티 매니저를 사용해서 엔티티를 영속성 컨텍스트에 등록한다. 트랜잭션을 커밋하기 전까지 DB에 엔티티를 저장하기 위한 Insert SQL를 실행하지 않는다.
내부 쿼리 저장소에 SQL를 모아두고 커밋하는 순간에 DB에 보내게 되는데, 이를 트랜잭션을 지원하는 쓰기 지연이라 한다.

```
val em = emf.createEntityManager()
val tx = em.transaction

tx.begin()

em.persist(member)

// DB에 Insert SQL를 보낸다.
tx.commit()
```

트랜잭션을 커밋하면 영속성 컨텍스트를 flush 한다. 영속성 컨텍스트에 있는 엔티티 변경 내용을 DB에 동기화 하는 작업이다.

## 엔티티 수정

### 변경 감지
JPA 엔티티를 수정할 때는 단순히 엔티티를 조회해서 데이터만 변경하면 된다. 엔티티의 변경사항을 DB에 자동으로 반영하는 기능을 변경 감지(dirty checking)이라고 한다.

트랜잭션을 커밋하면 내부적으로 flush가 호출되고 영속성 컨테스트에 보관되어 있는 엔티티와 변경된 엔티티를 비교한다. 변경된 부분을 반영 하기 위한 수정 쿼리를 생성해서 쓰기 지연 SQL 저장소에 보낸다. 

변경 감지는 영속성 컨텍스트가 관리하는 영속 상태의 엔티티에만 적용된다.

```
val em = emf.createEntityManager()
val tx = em.transaction
tx.begin()

val findMember = em.find(Member::class.java, memberId)

findMember.age = 20

tx.commit()
```

## 엔티티 삭제
엔티티를 삭제하려면 먼저 엔티티를 조회해야 한다. 엔티티 삭제도 즉시 DB에 반영하는 것이 아닌 쓰기 지연 SQL 저장소에 등록한다. flush 호출하면 DB에 삭제 쿼리를 전달하게 된다.

```
val findMember = em.find(Member::class.java, memberId)
em.remove(findMember) 
```

## 플러시
플러시(flush)는 영속성 컨텍스트의 변경 내용을 DB에 반영해서 동기화 하는 것이다. 영속성 컨텍스트를 플러시하는 방법은 3가지이다.

1. em.flush()를 직접 호출
2. 트랜잭션 커밋 시 플러시가 자동으로 호출
3. JPQL 쿼리 실행 시 플러시가 자동으로 호출 

### JPA 플러시 모드 옵션
- FlushModeType.AUTO: 커밋이나 쿼리를 실행할 때 플러시 (기본값)
- FLushModeType.COMMIT: 커밋할 때만 플러시

### Hibernate 플러시 옵션
JPA는 Flush 옵션을 2개만 지원하지만, Hiberante는 4개의 옵션을 제공한다.

- ALWAYS
- AUTO: 커밋이나 JPQL, Native 쿼리를 실행하기 전에 플러시 (기본값)
- COMMIT
- MANUAL

## 준영속
영속성 컨텍스트에 있던 영속 상태의 엔티티가 분리된 것을 준영속 상태라고 한다. 영속 상태의 엔티티를 준영속 상태로 만드는 방법은 다음 3가지이다.

1. em.detach(entity)
2. em.clear()
3. em.close()

### 병합 (merge)
준영속 상태의 엔티티를 다시 영속 상태로 변경하려면 병합을 사용하면 된다. merge 메서드는 준영속 상태의 엔티티를 받아서 새로운 영속 상태의 엔티티를 반환한다.

```
public <T> T merge(T entity);
```

병합은 비영속 엔티티도 영속 상태로 만들 수 있다. 병합 메서드에 넘어온 엔티티의 식별자 값으로 영속성 컨텍스트에 있는지 조회하고, 찾는 엔티티가 없으면 DB에서 조회한다.
DB에서도 찾지 못하면 새로운 엔티티를 생성해서 병합한다. 병합은 준영속, 비영속 상태를 신경쓰지 않는다. 엔티티가 존재하면 병합하고, 조회할 수 없으면 새로 생성한다.