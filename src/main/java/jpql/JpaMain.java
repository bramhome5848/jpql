package jpql;

import javax.persistence.*;
import java.util.Collection;
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
            //function8(em);
            //function9(em);
            //function10(em);
            //function11(em);
            //function12(em);
            function13(em);

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

    //경로 표현식
    public static void function9(EntityManager em) {

        Team team = new Team();
        team.setName("team1");
        em.persist(team);

        Member member1 = new Member();
        member1.setUserName("관리자1");
        member1.setTeam(team);
        em.persist(member1);

        Member member2 = new Member();
        member2.setUserName("관리자2");
        member2.setTeam(team);
        em.persist(member2);

        em.flush();
        em.clear();

        //상태필드 검색 -> 경로 탐색의 끝 -> 탐색 종료
        String query1 = "select m.userName from Member m";
        List<String> result1 = em.createQuery(query1, String.class)
                .getResultList();

        //단일 값 연관 경로 -> 묵시적 조인 발생 -> 탐색O
        //실무 사용시에 조심 -> 튜닝시에 어려움이 있음
        String query2 = "select m.team from Member m";
        List<Team> result2 = em.createQuery(query2, Team.class)
                .getResultList();

        //컬렉션 값 연관 경로 -> 묵시적 내부조인 발생, 탐색X
        //쿼리 내에서 컬렉션 각각에 대한 접근 불가, 컬렉션 자체에 대한 접근은 가능
        //따라서 명시적인 Join을 사용해야함 -> query4처럼 사용해야 접근가능
        String query3 = "select t.members from Team t";
        Collection result3 = em.createQuery(query3, Collection.class)
                .getResultList();

        String query4 = "select m.userName from Team t join t.members m";
        List<String> result4 = em.createQuery(query4, String.class)
                .getResultList();

        for (String s : result4) {
            System.out.println("s = " + s);
        }

        //결론 -> 묵시적 조인이 일어나는 방법은 사용하지 말아야 한다
        //실제 쿼리 튜닝 해야할 경우 확인이 어렵다
        //묵시적 조인은 -> 내부 조인만 가능
    }

    //***** fetch Join *****
    //jpql에서 성능 최적화를 위해 제공하는 기능
    //연관된 엔티티나 컬렉션을 SQL 한 번에 함께 조회하는 기능 -> 일종의 한 방 쿼리
    public static void function10(EntityManager em) {

        Team team1 = new Team();
        team1.setName("팀A");
        em.persist(team1);

        Team team2 = new Team();
        team2.setName("팀B");
        em.persist(team2);

        Team team3 = new Team();
        team3.setName("팀C");
        em.persist(team3);

        Member member1 = new Member();
        member1.setUserName("회원1");
        member1.setTeam(team1);
        em.persist(member1);

        Member member2 = new Member();
        member2.setUserName("회원2");
        member2.setTeam(team1);
        em.persist(member2);

        Member member3 = new Member();
        member3.setUserName("회원3");
        member3.setTeam(team2);
        em.persist(member3);

        Member member4 = new Member();
        member4.setUserName("회원4");
        em.persist(member4);

        em.flush();
        em.clear();

        //String query1 = "select m from Member m";
        //fetch join
        String query1 = "select m from Member m join fetch m.team";

        List<Member> members = em.createQuery(query1, Member.class)
                .getResultList();

        for (Member member : members) {
            System.out.println("member = " + member.getUserName() + ", " + member.getTeam().getName());
            //회원1, 팀A(SQL)
            //회원2, 팀A(1차 캐시)
            //회원3, 팀B(SQL)
            //회원 100명 조회 -> 최악의 경우 100명에 대한 팀을 가지고 오는 쿼리가 100번발생 -> 1 + N 문제 발생
            //단건의 경우 -> LZAY : 1개 조회 -> 조회 + 관련 데이터 조회 -> N+1 발생, EAGER -> 1개 조회 -> 조회
            //다건의 경우 -> LAZY : N개 조회 -> 조회 + 관련 데이터 조회 -> N+1 발생, EAGER -> N개 조회 -> 조회 + 관련 데이터 조회 -> N+1 문제 발생
            //fetch join -> 연간관계에 대한 필요 데이터를 한방 조회를 수행 -> 1번만 수행 -> 단건, 다건의 경우에 모두 커버 가능
            //지연로딩이라도 -> fetch Join이 우선으로 실행됨
        }

        //1:N의 경우
        //1:N에 대한 조회시 데이터가 뻥튀기 될 수 있음
        //1개에 조인되는 여러개의 데이터만큼 row가 출력됨
        //jpql에 distinct는 중복된 결과를 제거
        //sql에 ditinct를 추가하거나 애플리케이션에서 엔티티 중복 제거
        //같은 식별자를 가진 Team 엔티티 제거
        //fetch join은 연관된 애들을 모두 다 가져오는 것 -> 걸러서 가져오고 싶겠지만 그러면 fetch join을 사용하면 안됨
        //1대 N의 경우 데이터가 뻥티기 되기 때문에 paging을 사용할 수 없음
        //Lazy사용시 N+1 발생해결 방법
        //-> fetch 조인 이외에 또는 @batchsize() 이용 (in절에 조건 걸림)하여 테이블 수만큼으로 줄일 수 있음

        //String query2 = "select t from Team t join fetch t.members";
        String query2 = "select distinct t from Team t join fetch t.members";
        List<Team> teams = em.createQuery(query2, Team.class)
                .getResultList();

        for (Team team : teams) {
            System.out.println("team = " + team.getName() + " | " + team.getMembers().size());
            for(Member member : team.getMembers()) {
                System.out.println("-> " + member);
            }
        }
    }

    //엔티티 직접 사용 쿼리
    public static void function11(EntityManager em) {

        Team team1 = new Team();
        team1.setName("팀A");
        em.persist(team1);

        Team team2 = new Team();
        team2.setName("팀B");
        em.persist(team2);

        Team team3 = new Team();
        team3.setName("팀C");
        em.persist(team3);

        Member member1 = new Member();
        member1.setUserName("회원1");
        member1.setTeam(team1);
        em.persist(member1);

        Member member2 = new Member();
        member2.setUserName("회원2");
        member2.setTeam(team1);
        em.persist(member2);

        Member member3 = new Member();
        member3.setUserName("회원3");
        member3.setTeam(team2);
        em.persist(member3);

        Member member4 = new Member();
        member4.setUserName("회원4");
        em.persist(member4);

        em.flush();
        em.clear();

        //엔티티 직접 사용 -> 식별자를 이용해서 검색함
        //String query = "select m from Member m where m = :member";
        //식별자 직접 사용
        String query1 = "select m from Member m where m.id = :memberId";

        Member findMember = em.createQuery(query1, Member.class)
                .setParameter("memberId" , member1.getId())
                .getSingleResult();

        System.out.println("findMember = " + findMember);

        //외래키 값 사용
        String query2 = "select m from Member m where m.team = :team";
        List<Member> memberList = em.createQuery(query2, Member.class)
                .setParameter("team", team1)
                .getResultList();

        for (Member member : memberList) {
            System.out.println("member = " + member.getUserName());
        }
    }

    public static void function12(EntityManager em) {

        Team team1 = new Team();
        team1.setName("팀A");
        em.persist(team1);

        Team team2 = new Team();
        team2.setName("팀B");
        em.persist(team2);

        Team team3 = new Team();
        team3.setName("팀C");
        em.persist(team3);

        Member member1 = new Member();
        member1.setUserName("회원1");
        member1.setTeam(team1);
        em.persist(member1);

        Member member2 = new Member();
        member2.setUserName("회원2");
        member2.setTeam(team1);
        em.persist(member2);

        Member member3 = new Member();
        member3.setUserName("회원3");
        member3.setTeam(team2);
        em.persist(member3);

        Member member4 = new Member();
        member4.setUserName("회원4");
        em.persist(member4);

        em.flush();
        em.clear();

        List<Member> resultList = em.createNamedQuery("Member.findByUserName", Member.class)
                .setParameter("userName", "회원1")
                .getResultList();

        for (Member member : resultList) {
            System.out.println("member = " + member);
        }
    }

    public static void function13(EntityManager em) {

        Member member1 = new Member();
        member1.setUserName("회원1");
        member1.setAge(21);
        em.persist(member1);

        Member member2 = new Member();
        member2.setUserName("회원2");
        member2.setAge(23);
        em.persist(member2);

        Member member3 = new Member();
        member3.setUserName("회원3");
        member3.setAge(31);
        em.persist(member3);

        //벌크 연산은 영속성 컨텍스트를 무시하고 데이터베이스에 직접 쿼리
        //벌크 연산을 먼저 실행
        //벌크 연산 수행 후 영속성 컨텍스트 초기화 -> 영속성에 있는 데이터와 실제 데이터의 동기화가 필요함
        //영향을 받은 데이터수
        int count = em.createQuery("update Member m set m.age = 20")
                .executeUpdate();

        System.out.println("count = " + count);

        //초기화 하지 않으면 영속성 컨텍스트의 값이 그대로 나옴
        //따라서 벌크 연산수행 후에는 em.clear(); 실행
        em.clear(); //위에 선언된 것들은 준영속이 되버림
        Member findMember = em.find(Member.class, member1.getId());
        System.out.println("findMember.getAge() = " + findMember.getAge());
    }
}
