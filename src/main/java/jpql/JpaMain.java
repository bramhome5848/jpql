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

            Member member = new Member();
            member.setUserName("member1");
            member.setAge(10);
            em.persist(member);

            //리턴 타입이 명확할 때
            /*
            TypedQuery<Member> query1 = em.createQuery("select m from Member m", Member.class);
            TypedQuery<String> query2 = em.createQuery("select m.userName from Member m", String.class);

            //타입 정보를 받을 수 없을 때
            Query query3 = em.createQuery("select m.userName, m.age from Member m");

            //결과가 하나 이상일 때, 리스트 반환
            List<Member> resultList = query1.getResultList();
            for (Member member1 : resultList) {
                System.out.println("member1  = " + member1 );
            }
            */

            //이름 기준 파라미터 사용, 위치 기준은 불편할 수 있음
            Member singleResult = em.createQuery("select m from Member m where m.userName = :userName", Member.class)
                    .setParameter("userName", "member1")
                    .getSingleResult();

            System.out.println("singleResult = " + singleResult.getUserName());

            tx.commit();    //이 시점에 영속성 컨텍스트에 있는 것들에 대해서 쿼리가 날라감
        } catch (Exception e) {
            tx.rollback();
        } finally {
            em.close();
        }

        emf.close();
    }
}
