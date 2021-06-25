package security.ttaallkk.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import security.ttaallkk.domain.Member;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findMemberByEmail(String email);

    Optional<Member> findMemberByDisplayName(String displayName);

    Optional<Member> findMemberByUid(String uid);
 
    @Query("select m from Member m join fetch m.roles where m.email = :email")
    Optional<Member> findMemberByEmailFetch(@Param("email") String email);

    Optional<Member> findMemberByEmailAndRefreshToken(String email, String refreshToken);

    Optional<Member> deleteByEmail(String email);
}
