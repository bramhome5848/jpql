package jpql;

import javax.persistence.*;
import java.util.List;

public class JpaMain {

    public static void main(String[] args) {
        //persistence.xml 파일에서 unit-name
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");

        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        tx.begin();

        try {

            //function1(em);
            //function2(em);
            //function3(em);
            //function4(em);
            //function5(em);
            //function6(em);
            //function7(em);
            function8(em);

            //빠진 부분
            //enu, 엔티티 타입 사용쿼리
            //jpql기본 함수 사용법

            tx.commit();    //이 시점에 영속성 컨텍스트에 있는 것들에 대해서 쿼리가 날라감
        } catch (Exception e) {
            tx.rollback();
        } finally {
            em.close();
        }

        emf.close();
    }

    //return type에 대한 예재
    private static void function1(EntityManager em) {

        Member member = new Member();
        member.setUserName("member1");
        member.setAge(10);
        em.persist(member);

        //리턴 타입이 명확할 때
        TypedQuery<Member> query1 = em.createQuery("select m from Member m", Member.class);
        TypedQuery<String> query2 = em.createQuery("select m.userName from Member m", String.class);

        //타입 정보를 받을 수 없을 때
        Query query3 = em.createQuery("select m.userName, m.age from Member m");

        //JPQL 실행 전에 무조건 flush()로 DB와의 싱크를 맞춘 다음에 JPQL 쿼리를 날리도록 설정 되어 있음.

        //결과가 하나 이상일 때, 리스트 반환
        List<Member> resultList = query1.getResultList();
        for (Member member1 : resultList) {
            System.out.println("member1  = " + member1 );
        }

        //결과가 단 하나일 때
        //결과가 없으면: javax.persistence.NoResultException
        //둘 이상이면: javax.persistence.NonUniqueResultException
        Member singleResult = query1.getSingleResult();
        System.out.println("singleResult = " + singleResult);
    }

    //parameter 사용 jpql
    private static void function2(EntityManager em) {

        Member member = new Member();
        member.setUserName("member1");
        member.setAge(10);
        em.persist(member);

        //이름 기준 파라미터 사용, 위치 기준은 불편할 수 있음
        //이름으로 사용하는 것이 편리할 수 있음
        Member singleResult = em.createQuery("select m from Member m where m.userName = :userName", Member.class)
                .setParameter("userName", "member1")
                .getSingleResult();

        System.out.println("singleResult = " + singleResult.getUserName());
    }

    //projection(select)
    private static void function3(EntityManager em) {

        Member member = new Member();
        member.setUserName("member1");
        member.setAge(10);
        em.persist(member);

        //타입이 명확안 경우 타입을 사용 -> 엔티티, 조인, 임베디드 사용방법 같음
        List<Member> resultList = em.createQuery("select m FROM Member m", Member.class)
                .getResultList();
        for (Member member1 : resultList) {
            System.out.println("member1 = " + member1);
        }

        //컬럼 내용을 선택할 경우(스칼라 타입) -> 타입을 정할 수 없기 때문에

        //Object로 받는 방법
        List<Object[]> resultList1 = em.createQuery("select m.userName, m.age FROM Member m")
                .getResultList();

        Object[] result1 = resultList1.get(0);
        System.out.println("userName = " + result1[0]);
        System.out.println("age = " + result1[0]);

        //new 명령어로 조회 -> 단순 값을 DTO로 바로 조회
        //dto는 패키지 명을 포함한 전체 클래스명 입력
        //순서와 타입이 일치하는 생성자 필요..
        List<MemberDto> resultList2 = em.createQuery("select new jpql.MemberDto(m.userName, m.age) FROM Member m", MemberDto.class)
                .getResultList();

        MemberDto memberDto = resultList2.get(0);
        System.out.println("memberDto = " + memberDto.getUserName());
        System.out.println("memberDto = " + memberDto.getAge());
    }

    //paging
    public static void function4(EntityManager em) {

        for(int i= 0 ; i<100 ; i++) {
            Member member = new Member();
            member.setUserName("member" + i);
            member.setAge(i);
            em.persist(member);
        }

        List<Member> resultList = em.createQuery("select m from Member m order by m.age desc", Member.class)
                .setFirstResult(1)    //조회시작 위치
                .setMaxResults(10)    //조회할 데이터수
                .getResultList();

        System.out.println("resultList.size = " + resultList.size());
        for (Member member : resultList) {
            System.out.println("member = " + member);
        }
    }

    //join쿼리
    public static void function5(EntityManager em) {

        Team team = new Team();
        team.setName("member1");
        em.persist(team);

        Member member = new Member();
        member.setUserName("member1");
        member.setAge(10);
        member.setTeam(team);

        em.persist(member);

        em.flush();
        em.clear();

        //즉시 로딩도 지연로딩과 같이 먼저 영속성 컨텍스트에서 찾고 없으면 데이터베이스에 쿼리를 날리게 된다.

        //String query = "select m from Member m inner join m.team t";    //innter join
        //String query = "select m from Member m left join m.team t";     //outer join
        String query = "select m from Member m, Team t where m.userName = t.name";     //seta join(막조인)

        List<Member> result = em.createQuery(query, Member.class)
                .getResultList();

        System.out.println("result.size() = " + result.size());

    }

    //서브쿼리
    //JPA는 WHERE, HAVING 절에서만 서브 쿼리 사용 가능
    //SELECT 절도 가능(하이버네이트에서 지원)
    public static void function6(EntityManager em) {

        Team team = new Team();
        team.setName("teamA");
        em.persist(team);

        Member member = new Member();
        member.setUserName("member1");
        member.setAge(10);
        member.setTeam(team);

        em.persist(member);

        em.flush();
        em.clear();

        String query = "select m from Member m where exists (select t from m.team t where t.name = 'teamA')";
        int result = 0;
        List<Member> resultList = em.createQuery(query, Member.class)
                .getResultList();

        for (Member member1 : resultList) {
            System.out.println("member1 = " + member1);
        }
    }

    //case
    public static void function7(EntityManager em) {

        Team team = new Team();
        team.setName("teamA");
        em.persist(team);

        Member member = new Member();
        member.setUserName("member1");
        member.setAge(10);
        member.setTeam(team);

        em.persist(member);

        em.flush();
        em.clear();

        String query =
                "select " +
                        "case when m.age <= 10 then '학생요금'" +
                        "     when m.age >= 10 then '경로요금'" +
                        "     else '일반요금' end "+
                "from Member m";

        List<String> result = em.createQuery(query, String.class)
                .getResultList();

        for (String s : result) {
            System.out.println("s = " + s);
        }
    }

    //조건식2
    public static void function8(EntityManager em) {

        Team team = new Team();
        team.setName("teamA");
        em.persist(team);

        Member member = new Member();
        member.setUserName("관리자1");
        member.setAge(10);
        member.setTeam(team);

        em.persist(member);

        em.flush();
        em.clear();

        //사용자 이름이 없으면 뒷 것을 반납 -> NVL
        //String query = "select coalesce(m.userName, '이름 없는 회원') as userName from Member m";

        //nullif -> 두 값이 같으면 null 반환, 다르면 첫번째 값 반환
        String query = "select nullif(m.userName, '관리자') as userName from Member m";
        List<String> result = em.createQuery(query, String.class)
                .getResultList();

        for (String s : result) {
            System.out.println("s = " + s);
        }
    }
}
